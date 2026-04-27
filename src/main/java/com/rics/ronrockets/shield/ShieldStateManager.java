package com.rics.ronrockets.shield;

import com.solegendary.reignofnether.building.BuildingPlacement;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ShieldStateManager {
    private static final int ACTIVE_DURATION = 200; // 10 seconds
    private static final Map<BuildingPlacement, Integer> activeTimer = new HashMap<>();

    public static boolean isActive(BuildingPlacement building) {
        return activeTimer.getOrDefault(building, 0) > 0;
    }

    public static void activate(BuildingPlacement building) {
        activeTimer.put(building, ACTIVE_DURATION);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<BuildingPlacement, Integer>> it = activeTimer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BuildingPlacement, Integer> entry = it.next();
            BuildingPlacement building = entry.getKey();
            int time = entry.getValue() - 1;

            if (time <= 0 || !building.isBuilt || building.shouldBeDestroyed()) {
                it.remove();
            } else {
                entry.setValue(time);
                // ✅ Spawns Enchanting particles while the shield is actively defending
                if (time % 5 == 0 && building.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, 
                        building.centrePos.getX() + 0.5, building.centrePos.getY() + 4.0, building.centrePos.getZ() + 0.5, 
                        40, 1.5, 1.5, 1.5, 0.1);
                }
            }
        }
    }
}
