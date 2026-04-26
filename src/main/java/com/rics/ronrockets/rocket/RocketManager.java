package com.rics.ronrockets.rocket;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RocketManager {

    private static final List<RocketStrike> ACTIVE_STRIKES = new ArrayList<>();

    public static void registerStrike(RocketStrike strike) {
        ACTIVE_STRIKES.add(strike);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = BuildingServerEvents.getServerLevel();
        if (level == null) return;

        long currentTick = level.getGameTime();

        Iterator<RocketStrike> iterator = ACTIVE_STRIKES.iterator();

        while (iterator.hasNext()) {
            RocketStrike strike = iterator.next();

            if (currentTick >= strike.impactTick) {
                resolveStrike(strike, level);
                iterator.remove();
            }
        }
    }

    private static void resolveStrike(RocketStrike strike, ServerLevel level) {

    int radius = 8;

    // ✅ Check Shield Interception FIRST
    for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

        if (!(building.getBuilding() instanceof ShieldArrayBuilding)) continue;
        if (!building.isBuilt) continue;

        double dist = building.centrePos.distSqr(strike.targetPos);

        if (dist <= ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) {

            // Check cooldown
            for (var ability : building.getAbilities()) {
                if (ability instanceof com.rics.ronrockets.ability.ShieldInterceptAbility) {

                    if (ability.isOffCooldown(building)) {
                        ability.setToMaxCooldown(building);
                        return; // ✅ Rocket intercepted, cancel strike
                    }
                }
            }
        }
    }

    // ✅ DAMAGE BUILDINGS
    for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

        if (!building.isBuilt) continue;

        double dist = building.centrePos.distSqr(strike.targetPos);
        if (dist > radius * radius) continue;

        float percent = building.isCapitol ? 0.40f : 0.80f;

        int blocksToDestroy = (int)(building.getBlocksTotal() * percent);

        building.destroyRandomBlocks(blocksToDestroy);

        if (building.shouldBeDestroyed()) {
            BuildingServerEvents.cancelBuilding(building, building.ownerName);
        }
    }

    // ✅ DAMAGE UNITS
    var area = new net.minecraft.world.phys.AABB(strike.targetPos).inflate(radius);

    var entities = level.getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            area
    );

    for (var entity : entities) {
        if (entity instanceof com.solegendary.reignofnether.unit.interfaces.Unit) {
            entity.hurt(level.damageSources().generic(), 50f);
          }
       }
    }
}
