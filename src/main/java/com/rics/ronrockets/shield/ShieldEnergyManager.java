package com.rics.ronrockets.shield;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ShieldEnergyManager {

    private static final int MAX_ENERGY = 500;
    private static final int REFILL_PER_TICK = 2;
    private static final int ORE_COST_PER_REFILL = 1;

    private static final Map<BuildingPlacement, Integer> energyMap = new HashMap<>();

    public static int getEnergy(BuildingPlacement placement) {
        return energyMap.getOrDefault(placement, MAX_ENERGY);
    }

    public static boolean consumeEnergy(BuildingPlacement placement, int amount) {
        int current = getEnergy(placement);
        if (current < amount) return false;
        energyMap.put(placement, current - amount);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {

            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;

            int energy = getEnergy(placement);

            if (energy >= MAX_ENERGY) continue;

            if (ResourcesServerEvents.canAfford(
                    placement.ownerName,
                    ResourceName.ORE,
                    ORE_COST_PER_REFILL)) {

                ResourcesServerEvents.addSubtractResources(
                        new Resources(placement.ownerName, 0, 0, -ORE_COST_PER_REFILL)
                );

                energyMap.put(placement,
                        Math.min(MAX_ENERGY, energy + REFILL_PER_TICK));
            }
        }
    }
}
