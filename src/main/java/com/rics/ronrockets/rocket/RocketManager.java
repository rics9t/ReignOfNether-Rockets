package com.rics.ronrockets.rocket;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.network.ScreenShakeClientboundPacket;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class RocketManager {

    private RocketManager() {
    }

    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
        int radius = 12;

        // ── Shield interception check ─────────────────────────────
        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;
            if (placement.ownerName.equals(strike.attacker)) continue;
            if (placement.centrePos.distSqr(strike.targetPos) > (double) ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS)
                continue;

            ShieldInterceptAbility shieldAbility = ShieldInterceptAbility.getFrom(placement);
            if (shieldAbility == null || !shieldAbility.isShieldActive(placement)) continue;

            ShieldInterceptAbility.spawnInterceptParticles(level, placement.centrePos, strike.targetPos);
            return; // rocket absorbed — skip damage and explosion
        }

        double cx = strike.targetPos.getX() + 0.5;
        double cy = strike.targetPos.getY() + 0.5;
        double cz = strike.targetPos.getZ() + 0.5;

        // ── Stage 1: Flash / initial burst ────────────────────────
        // Bright flash at impact center
        level.sendParticles(ParticleTypes.FLASH, cx, cy, cz,
                1, 0, 0, 0, 0);

        // ── Stage 2: Shockwave ring ───────────────────────────────
        // EXPLOSION_EMITTER creates the expanding ring effect
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz,
                3, 0.5, 0.5, 0.5, 0);

        // ── Stage 3: Core explosion ───────────────────────────────
        level.sendParticles(ParticleTypes.EXPLOSION, cx, cy + 0.5, cz,
                5, 1.0, 1.0, 1.0, 0.02);

        // ── Stage 4: Smoke cloud ──────────────────────────────────
        // Dense campfire smoke for lingering cloud
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy, cz,
                180, 4.0, 3.0, 4.0, 0.08);

        // Lighter large smoke for volume
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy, cz,
                100, 5.0, 4.0, 5.0, 0.05);

        // ── Stage 5: Fire & debris ────────────────────────────────
        // Lava particles for burning debris
        level.sendParticles(ParticleTypes.LAVA, cx, cy + 1, cz,
                40, 3.0, 2.0, 3.0, 0.5);

        // Flame burst
        level.sendParticles(ParticleTypes.FLAME, cx, cy + 0.5, cz,
                80, 3.0, 2.0, 3.0, 0.3);

        // Ember sparks flying outward
        level.sendParticles(ParticleTypes.FLAME, cx, cy + 1, cz,
                30, 6.0, 4.0, 6.0, 0.8);

        // ── Stage 6: Ground-level dust ring ───────────────────────
        level.sendParticles(ParticleTypes.CLOUD, cx, cy - 0.5, cz,
                60, 6.0, 0.2, 6.0, 0.04);

        // ── Sound ─────────────────────────────────────────────────
        level.playSound(null, cx, cy, cz,
                SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 6.0f, 0.8f);
        // Secondary deeper boom
        level.playSound(null, cx, cy, cz,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.MASTER, 3.0f, 0.4f);

        // ── Screen shake ──────────────────────────────────────────
        ScreenShakeClientboundPacket.send(strike.targetPos, 6.0f, 30);

        // ── Attack warnings for owners near impact ────────────────
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;
            if (building.ownerName.equals(strike.attacker)) continue; // skip attacker
            if (building.centrePos.distSqr(strike.targetPos) <= (double) radius * radius) {
                AttackWarningClientboundPacket.sendWarning(building.ownerName, strike.targetPos);
            }
        }

        // ── Damage buildings (block destruction) ──────────────────
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;

            double distSqr = building.centrePos.distSqr(strike.targetPos);
            if (distSqr > (double) radius * radius) continue;

            double falloff = Math.max(0, 1.0 - (Math.sqrt(distSqr) / radius));
            float maxPercent = building.isCapitol ? 0.30f : 0.60f;

            int blocksToDestroy = (int) (building.getBlocksTotal() * maxPercent * falloff);

            if (blocksToDestroy > 0) {
                building.destroyRandomBlocks(blocksToDestroy);
                if (building.shouldBeDestroyed()) {
                    BuildingServerEvents.cancelBuilding(building, building.ownerName);
                }
            }
        }

        // ── Damage units ──────────────────────────────────────────
        AABB area = new AABB(strike.targetPos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (!(entity instanceof Unit)) continue;

            double dist = Math.sqrt(entity.distanceToSqr(cx, cy, cz));
            if (dist > radius) continue;

            double falloff = 1.0 - (dist / radius);
            float damage = (float) (200 * falloff);

            if (damage > 0) {
                entity.hurt(level.damageSources().generic(), damage);
            }
        }
    }
}
