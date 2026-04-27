package com.rics.ronrockets.shield;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ShieldEnergyManager {

    private static final int MAX_ENERGY = 500;
    private static final int INTERCEPT_COST = 250;
    private static final int REFILL_AMOUNT = 5;
    private static final int REFILL_COST = 2;

    private static final Map<BuildingPlacement, Integer> energyMap = new HashMap<>();

    public static int getEnergy(BuildingPlacement building) {
        return energyMap.getOrDefault(building, MAX_ENERGY);
    }

    public static void consumeEnergy(BuildingPlacement building) {
        energyMap.put(building, Math.max(0, getEnergy(building) - INTERCEPT_COST));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!placement.isBuilt) continue;

            int energy = getEnergy(placement);

            // ✅ Spawn particle indicator ONLY for the owner if out of energy
            if (energy < INTERCEPT_COST) {
                ServerPlayer owner = event.getServer().getPlayerList().getPlayerByName(placement.ownerName);
                if (owner != null) {
                    ((ServerLevel) owner.level()).sendParticles(owner, ParticleTypes.SMOKE, false, 
                        placement.centrePos.getX() + 0.5, placement.centrePos.getY() + 3, placement.centrePos.getZ() + 0.5, 
                        2, 0.2, 0.2, 0.2, 0.02);
                }
            }

            if (energy >= MAX_ENERGY) continue;

            if (ResourcesServerEvents.canAfford(placement.ownerName, ResourceName.ORE, REFILL_COST)) {
                ResourcesServerEvents.addSubtractResources(new com.solegendary.reignofnether.resources.Resources(placement.ownerName, 0, 0, -REFILL_COST));
                energyMap.put(placement, Math.min(MAX_ENERGY, energy + REFILL_AMOUNT));
            }
        }
    }
}
