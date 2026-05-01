package com.rics.ronrockets.shield;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShieldVisualTickHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!placement.isBuilt) continue;
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!(placement.getLevel() instanceof ServerLevel serverLevel)) continue;

            ShieldInterceptAbility shield = null;
            for (var a : placement.getAbilities()) {
                if (a instanceof ShieldInterceptAbility s) {
                    shield = s;
                    break;
                }
            }
            if (shield == null) continue;

            float healthPct = ShieldInterceptAbility.getHealthPercent(placement);

            // Smoke particles when building is damaged (not at full health)
            if (healthPct < 1.0f && placement.tickAge % 8 == 0) {
                double cx = placement.centrePos.getX() + 0.5;
                double cy = placement.centrePos.getY() + 2.0;
                double cz = placement.centrePos.getZ() + 0.5;
                // More smoke the more damaged it is
                int count = (int) ((1.0f - healthPct) * 6) + 1;
                serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx, cy, cz,
                    count, 1.5, 0.5, 1.5, 0.02
                );
                // Some flames for heavily damaged
                if (healthPct < 0.4f && placement.tickAge % 16 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        cx, cy, cz,
                        3, 0.8, 0.3, 0.8, 0.04
                    );
                }
            }

            // Enchant particles when shield is currently active
            if (shield.isShieldActive(placement) && placement.tickAge % 5 == 0) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    placement.centrePos.getX() + 0.5,
                    placement.centrePos.getY() + 3.0,
                    placement.centrePos.getZ() + 0.5,
                    25, 1.5, 1.0, 1.5, 0.08
                );
            }
        }
    }
}
