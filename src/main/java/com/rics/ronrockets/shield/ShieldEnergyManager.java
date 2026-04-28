package com.rics.ronrockets.shield;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ShieldEnergyManager {

    private static final Map<BlockPos, Integer> energyByOrigin = new HashMap<>();
    private static final int FULL_SYNC_INTERVAL = 20;

    public static final int MAX_ENERGY = 500;
    public static final int REFILL_PER_TICK = 2;
    public static final int ORE_COST_PER_REFILL = 1;

    public static int getEnergy(BuildingPlacement placement) {
        return getEnergy(placement.originPos);
    }

    public static int getEnergy(BlockPos originPos) {
        return energyByOrigin.getOrDefault(originPos, MAX_ENERGY);
    }

    public static int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public static void setEnergy(BuildingPlacement placement, int amount) {
        setEnergy(placement.originPos, amount);
    }

    public static void setEnergy(BlockPos originPos, int amount) {
        energyByOrigin.put(originPos.immutable(), Math.max(0, Math.min(MAX_ENERGY, amount)));
    }

    public static boolean consumeEnergy(BuildingPlacement placement, int amount) {
        int current = getEnergy(placement);
        if (current < amount) {
            return false;
        }
        setEnergy(placement, current - amount);
        return true;
    }

    public static void syncShieldState(BuildingPlacement placement) {
        ShieldEnergyClientboundPacket.syncShieldState(placement);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        Map<BlockPos, Boolean> activeOrigins = new HashMap<>();

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {

            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;

            activeOrigins.put(placement.originPos.immutable(), Boolean.TRUE);

            int energy = getEnergy(placement);

            if (energy < MAX_ENERGY &&
                ResourcesServerEvents.canAfford(
                        placement.ownerName,
                        ResourceName.ORE,
                        ORE_COST_PER_REFILL)) {

                ResourcesServerEvents.addSubtractResources(
                        new Resources(placement.ownerName, 0, 0, -ORE_COST_PER_REFILL)
                );

                setEnergy(placement, energy + REFILL_PER_TICK);
                syncShieldState(placement);
            } else if (placement.tickAge % FULL_SYNC_INTERVAL == 0) {
                syncShieldState(placement);
            }
        }

        energyByOrigin.keySet().removeIf(origin -> !activeOrigins.containsKey(origin));
    }
}
