package com.rics.ronrockets.building;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class RocketPlacementHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        Map<String, Integer> siloCount = new HashMap<>();

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {

            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;
            if (!placement.isBuilt) continue;

            siloCount.put(
                    placement.ownerName,
                    siloCount.getOrDefault(placement.ownerName, 0) + 1
            );
        }

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {

            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;
            if (!placement.isBuilt) continue;

            if (siloCount.getOrDefault(placement.ownerName, 0) > 1) {
                BuildingServerEvents.cancelBuilding(placement, placement.ownerName);
            }
        }
    }
}
