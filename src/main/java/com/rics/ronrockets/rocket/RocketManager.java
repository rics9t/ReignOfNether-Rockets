package com.rics.ronrockets.rocket;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.network.ScreenShakeClientboundPacket;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;

import net.minecraft.core.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class RocketManager {

private RocketManager() { }

/**
 * Handles rocket impact logic:
 * 1. If shield is ACTIVE (ability running, 5 sec window), intercept rocket mid-air
 * 2. If shield not active, rocket impacts normally
 */
public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
    int warningRadius = 64; // Warning radius
    int damageRadius = 12; // Damage radius (nerfed)

    // Check for active shields that can intercept
    for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
        if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
        if (!placement.isBuilt) continue;
        if (placement.ownerName.equals(strike.attacker)) continue;

        double distSq = placement.centrePos.distSqr(strike.targetPos);
        if (distSq > (double) ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) continue;

        ShieldInterceptAbility shieldAbility = ShieldInterceptAbility.getFrom(placement);
        if (shieldAbility == null) continue;

        // Only intercept when shield is ACTIVELY running (not just ready)
        boolean isActive = shieldAbility.isShieldActive(placement);
        if (!isActive) continue;

        // Shield is active - intercept the rocket!
        ShieldInterceptAbility.spawnInterceptParticles(level, placement.centrePos, strike.targetPos);
        return; // Rocket intercepted, don't impact
    }

    // No intercept - rocket impacts normally
    double cx = strike.targetPos.getX() + 0.5;
    double cy = strike.targetPos.getY() + 0.5;
    double cz = strike.targetPos.getZ() + 0.5;

    // Visual effects
    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0, 0, 0, 0);
    level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy - 0.5, cz, 40, 6.0, 0.3, 6.0, 0.04);
    level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 1, cz, 30, 3.0, 2.0, 3.0, 0.05);
    level.playSound(null, cx, cy, cz, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 6.0f, 0.7f);
    ScreenShakeClientboundPacket.send(strike.targetPos, 5.0f, 25);

    // Attack warnings - send to all players who own buildings near impact
    for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
        if (!building.isBuilt) continue;
        if (building.ownerName.equals(strike.attacker)) continue;
        if (building.centrePos.distSqr(strike.targetPos) <= (double) warningRadius * warningRadius) {
            AttackWarningClientboundPacket.sendWarning(building.ownerName, strike.targetPos);
        }
    }

    // Damage buildings
    List<BuildingPlacement> buildingsToDamage = new ArrayList<>();
    for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
        if (!building.isBuilt) continue;
        double distSqr = building.centrePos.distSqr(strike.targetPos);
        if (distSqr > (double) damageRadius * damageRadius) continue;
        buildingsToDamage.add(building);
    }
    for (BuildingPlacement building : buildingsToDamage) {
        double distSqr = building.centrePos.distSqr(strike.targetPos);
        double falloff = Math.max(0, 1.0 - (Math.sqrt(distSqr) / damageRadius));
        float maxPercent = building.isCapitol ? 0.40f : 0.80f;
        int blocksToDestroy = (int) (building.getBlocksTotal() * maxPercent * falloff);
        if (blocksToDestroy > 0) {
            building.destroyRandomBlocks(blocksToDestroy);
            if (building.shouldBeDestroyed()) {
                BuildingServerEvents.cancelBuilding(building, building.ownerName);
            }
        }
    }

    // Damage units
    AABB area = new AABB(strike.targetPos).inflate(damageRadius);
    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
    for (LivingEntity entity : entities) {
        if (!(entity instanceof Unit)) continue;
        double dist = Math.sqrt(entity.distanceToSqr(cx, cy, cz));
        if (dist > damageRadius) continue;
        double falloff = 1.0 - (dist / damageRadius);
        float damage = (float) (180 * falloff);
        if (damage > 0) {
            entity.hurt(level.damageSources().generic(), damage);
        }
    }
}
}
