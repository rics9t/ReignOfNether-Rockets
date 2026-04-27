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

            if (!shield.isShieldActive(placement)) continue;

            if (placement.tickAge % 5 != 0) continue;

            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    placement.centrePos.getX() + 0.5,
                    placement.centrePos.getY() + 3.0,
                    placement.centrePos.getZ() + 0.5,
                    25,
                    1.5, 1.0, 1.5,
                    0.08
            );
        }
    }
}
