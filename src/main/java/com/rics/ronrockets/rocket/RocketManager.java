package com.rics.ronrockets.rocket;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.shield.ShieldStateManager;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RocketManager {

    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {

        int radius = 8;

        // ✅ Dome-style interception
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

            if (!(building.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);

            if (dist <= ShieldArrayBuilding.SHIELD_RADIUS *
                    ShieldArrayBuilding.SHIELD_RADIUS) {

                if (ShieldStateManager.isActive(building)) {
                    return; // rocket destroyed inside dome
                }
            }
        }

        // ✅ Native warning
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);
            if (dist <= radius * radius) {
                AttackWarningClientboundPacket.sendWarning(
                        building.ownerName,
                        strike.targetPos
                );
            }
        }

        // ✅ Damage buildings
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);
            if (dist > radius * radius) continue;

            float percent = building.isCapitol ? 0.40f : 0.80f;

            int blocksToDestroy =
                    (int)(building.getBlocksTotal() * percent);

            building.destroyRandomBlocks(blocksToDestroy);

            if (building.shouldBeDestroyed()) {
                BuildingServerEvents.cancelBuilding(
                        building,
                        building.ownerName
                );
            }
        }

        // ✅ Damage units
        AABB area = new AABB(strike.targetPos).inflate(radius);

        List<LivingEntity> entities =
                level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity instanceof Unit) {
                entity.hurt(level.damageSources().generic(), 50f);
            }
        }
    }
}
