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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RocketManager {

    private RocketManager() { }

    private static final int WARNING_RADIUS = 64;
    private static final int DAMAGE_RADIUS = 10;

    /**
     * Handles rocket impact logic:
     * 1. If an active shield is in range, intercept (safety net — primary intercept is in RocketEntity.tick)
     * 2. Otherwise, full impact with damage and effects
     */
    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
        // Safety-net: check for active shields at impact point
        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;
            if (placement.ownerName.equals(strike.attacker)) continue;

            double distSq = placement.centrePos.distSqr(strike.targetPos);
            if (distSq > (double) ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) continue;

            ShieldInterceptAbility shieldAbility = ShieldInterceptAbility.getFrom(placement);
            if (shieldAbility == null) continue;

            if (shieldAbility.isShieldActive(placement)) {
                ShieldInterceptAbility.spawnInterceptParticles(level, placement.centrePos, strike.targetPos);
                return;
            }
        }

        // Full impact
        double cx = strike.targetPos.getX() + 0.5;
        double cy = strike.targetPos.getY() + 0.5;
        double cz = strike.targetPos.getZ() + 0.5;

        // Visual effects
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy - 0.5, cz, 40, 6.0, 0.3, 6.0, 0.04);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 1, cz, 30, 3.0, 2.0, 3.0, 0.05);
        level.sendParticles(ParticleTypes.FLAME, cx, cy, cz, 25, 4.0, 1.0, 4.0, 0.06);
        level.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 6.0f, 0.7f);
        ScreenShakeClientboundPacket.send(strike.targetPos, 5.0f, 25);

        // Attack warnings — collect unique owner names first
        Set<String> warnedOwners = new HashSet<>();
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;
            if (building.ownerName.equals(strike.attacker)) continue;
            if (building.centrePos.distSqr(strike.targetPos) <= (double) WARNING_RADIUS * WARNING_RADIUS) {
                if (warnedOwners.add(building.ownerName)) {
                    AttackWarningClientboundPacket.sendWarning(building.ownerName, strike.targetPos);
                }
            }
        }

        // Damage buildings — snapshot then process
        List<BuildingPlacement> buildingsToDamage = new ArrayList<>();
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;
            double distSqr = building.centrePos.distSqr(strike.targetPos);
            if (distSqr > (double) DAMAGE_RADIUS * DAMAGE_RADIUS) continue;
            buildingsToDamage.add(building);
        }
        // Collect cancelled buildings separately to avoid ConcurrentModificationException
        List<BuildingPlacement> toCancel = new ArrayList<>();
        for (BuildingPlacement building : buildingsToDamage) {
            double distSqr = building.centrePos.distSqr(strike.targetPos);
            double falloff = Math.max(0, 1.0 - (Math.sqrt(distSqr) / DAMAGE_RADIUS));
            float maxPercent = building.isCapitol ? 0.35f : 0.65f;
            int blocksToDestroy = (int) (building.getBlocksTotal() * maxPercent * falloff);
            if (blocksToDestroy > 0) {
                building.destroyRandomBlocks(blocksToDestroy);
                if (building.shouldBeDestroyed()) {
                    toCancel.add(building);
                }
            }
        }
        for (BuildingPlacement building : toCancel) {
            BuildingServerEvents.cancelBuilding(building, building.ownerName);
        }

        // Damage units
        AABB area = new AABB(strike.targetPos).inflate(DAMAGE_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            if (!(entity instanceof Unit)) continue;
            double dist = Math.sqrt(entity.distanceToSqr(cx, cy, cz));
            if (dist > DAMAGE_RADIUS) continue;
            double falloff = 1.0 - (dist / DAMAGE_RADIUS);
            float damage = (float) (120 * falloff);
            if (damage > 0) {
                entity.hurt(level.damageSources().generic(), damage);
            }
        }
    }
}
