package com.rics.ronrockets.building;

import com.rics.ronrockets.RonRocketsConfig;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.sandbox.SandboxServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Safety-net handler that removes excess silos beyond the configured limit.
 * The primary restriction is in AbstractRocketSilo.checkOnePerPlayerAndCreate.
 */
public class RocketPlacementHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Count silos per player
        Map<String, Integer> siloCount = new HashMap<>();
        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (placement.getBuilding() instanceof AbstractRocketSilo) {
                siloCount.merge(placement.ownerName, 1, Integer::sum);
            }
        }

        // Collect excess silos to remove (avoid ConcurrentModificationException)
        List<BuildingPlacement> toRemove = new ArrayList<>();
        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;

            String owner = placement.ownerName;
            if (SandboxServer.isSandboxPlayer(owner)) continue;

            int limit = RonRocketsConfig.getSiloLimit();
            int count = siloCount.getOrDefault(owner, 0);
            if (count > limit) {
                toRemove.add(placement);
                siloCount.put(owner, count - 1);
            }
        }

        // Remove after iteration
        for (BuildingPlacement placement : toRemove) {
            BuildingServerEvents.cancelBuilding(placement, placement.ownerName);
        }
    }
}
