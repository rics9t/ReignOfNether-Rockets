package com.rics.ronrockets.building;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Safety-net handler that removes any duplicate silos that slip through
 * the createBuildingPlacement guard (e.g. due to race conditions on tick).
 * The primary restriction is in AbstractRocketSilo.checkOnePerPlayerAndCreate.
 */
public class RocketPlacementHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Count silos per player (built or under construction)
        Map<String, Integer> siloCount = new HashMap<>();

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (placement.getBuilding() instanceof AbstractRocketSilo) {
                siloCount.merge(placement.ownerName, 1, Integer::sum);
            }
        }

        // If any player has more than one, remove the extras (keep the first)
        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;

            int count = siloCount.getOrDefault(placement.ownerName, 0);
            if (count > 1) {
                // Remove the duplicate and decrement so only one is removed per extra
                BuildingServerEvents.cancelBuilding(placement, placement.ownerName);
                siloCount.put(placement.ownerName, count - 1);
            }
        }
    }
}
