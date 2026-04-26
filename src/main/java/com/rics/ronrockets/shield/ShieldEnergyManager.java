package com.rics.ronrockets.shield;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ShieldEnergyManager {

    private static final int MAX_ENERGY = 500;
    private static final int INTERCEPT_COST = 250;
    private static final int REFILL_AMOUNT = 10;
    private static final int REFILL_COST = 2;

    private static final Map<BuildingPlacement, Integer> energyMap = new HashMap<>();

    public static int getEnergy(BuildingPlacement building) {
        return energyMap.getOrDefault(building, MAX_ENERGY);
    }

    public static void consumeEnergy(BuildingPlacement building) {
        int energy = getEnergy(building);
        energyMap.put(building, Math.max(0, energy - INTERCEPT_COST));
    }

    public static boolean canIntercept(BuildingPlacement building) {
        return getEnergy(building) >= INTERCEPT_COST;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {

            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;

            int energy = getEnergy(placement);
            if (energy >= MAX_ENERGY) continue;

            // Try to consume ore from owner
            if (ResourcesServerEvents.canAfford(
                    placement.ownerName,
                    ResourceName.ORE,
                    REFILL_COST)) {

                ResourcesServerEvents.addSubtractResources(
                        new com.solegendary.reignofnether.resources.Resources(
                                placement.ownerName,
                                0, 0, -REFILL_COST
                        )
                );

                energyMap.put(placement,
                        Math.min(MAX_ENERGY, energy + REFILL_AMOUNT));
            }
        }
    }
}
