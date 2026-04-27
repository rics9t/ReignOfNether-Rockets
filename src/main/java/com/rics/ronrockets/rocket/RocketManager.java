package com.rics.ronrockets.rocket;

import com.rics.ronrockets.building.AbstractRocketSilo;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.shield.ShieldStateManager;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocketManager {

    // ✅ Custom Trackers for Rocket Production (Bypasses RoN engine limitations)
    public static Map<BlockPos, Integer> storedRockets = new HashMap<>();
    public static Map<BlockPos, Integer> productionTicks = new HashMap<>();
    public static Map<BlockPos, Integer> cooldownTicks = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;
            if (!placement.isBuilt) continue;

            BlockPos pos = placement.centrePos;
            int stored = storedRockets.getOrDefault(pos, 0);
            int prod = productionTicks.getOrDefault(pos, 0);
            int cool = cooldownTicks.getOrDefault(pos, 0);

            // Production Timer
            if (prod > 0) {
                prod--;
                if (prod <= 0) {
                    stored = Math.min(2, stored + 1);
                    cool = 3600; // ✅ Starts 3 Minute Cooldown
                }
                productionTicks.put(pos, prod);
                storedRockets.put(pos, stored);
            }

            // Cooldown Timer
            if (cool > 0) {
                cool--;
                cooldownTicks.put(pos, cool);
            }
        }
    }

    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
        int radius = 12; // ✅ 12 Block Radius AOE

        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!(building.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!building.isBuilt) continue;
            if (building.centrePos.distSqr(strike.targetPos) <= ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) {
                if (ShieldStateManager.isActive(building)) return; // Intercepted
            }
        }

        // Send Warnings
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;
            if (building.centrePos.distSqr(strike.targetPos) <= radius * radius) {
                AttackWarningClientboundPacket.sendWarning(building.ownerName, strike.targetPos);
            }
        }

        // ✅ Damage buildings (Linear Falloff)
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;

            double distSqr = building.centrePos.distSqr(strike.targetPos);
            if (distSqr > radius * radius) continue;

            double falloff = 1.0 - (Math.sqrt(distSqr) / radius);
            float maxPercent = building.isCapitol ? 0.30f : 0.60f;

            int blocksToDestroy = (int) (building.getBlocksTotal() * maxPercent * falloff);

            if (blocksToDestroy > 0) {
                building.destroyRandomBlocks(blocksToDestroy);
                if (building.shouldBeDestroyed()) {
                    BuildingServerEvents.cancelBuilding(building, building.ownerName);
                }
            }
        }

        // ✅ Damage units (Linear Falloff)
        AABB area = new AABB(strike.targetPos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity instanceof Unit) {
                double dist = entity.distanceToSqr(strike.targetPos.getX(), strike.targetPos.getY(), strike.targetPos.getZ());
                if (dist <= radius * radius) {
                    double falloff = 1.0 - (Math.sqrt(dist) / radius);
                    float damage = (float) (200 * falloff); // Max 200 damage at epicenter
                    if (damage > 0) entity.hurt(level.damageSources().generic(), damage);
                }
            }
        }
    }
}
