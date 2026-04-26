package com.rics.ronrockets.shield;

import com.solegendary.reignofnether.building.BuildingPlacement;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ShieldStateManager {

    private static final int ACTIVE_DURATION = 200; // 10 seconds
    private static final int COOLDOWN_DURATION = 600; // 30 seconds

    private static final Map<BuildingPlacement, Integer> activeTimer = new HashMap<>();
    private static final Map<BuildingPlacement, Integer> cooldownTimer = new HashMap<>();

    public static boolean isActive(BuildingPlacement building) {
        return activeTimer.getOrDefault(building, 0) > 0;
    }

    public static boolean canActivate(BuildingPlacement building) {
        return cooldownTimer.getOrDefault(building, 0) <= 0
                && activeTimer.getOrDefault(building, 0) <= 0;
    }

    public static void activate(BuildingPlacement building) {
        activeTimer.put(building, ACTIVE_DURATION);
        cooldownTimer.put(building, COOLDOWN_DURATION);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement building : activeTimer.keySet()) {

            int active = activeTimer.getOrDefault(building, 0);
            if (active > 0) {
                activeTimer.put(building, active - 1);
            }

            int cooldown = cooldownTimer.getOrDefault(building, 0);
            if (cooldown > 0) {
                cooldownTimer.put(building, cooldown - 1);
            }
        }
    }
}
