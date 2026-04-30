package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.rics.ronrockets.rocket.RocketProd;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ActiveProduction;
import com.solegendary.reignofnether.building.production.ProductionItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;

/**
 * Custom ProductionPlacement that enforces the rocket queue limit on the server side.
 * Prevents players from queuing more rockets than the storage limit allows.
 */
public class RocketSiloPlacement extends ProductionPlacement {

    public RocketSiloPlacement(Building building, Level level, BlockPos originPos,
                               Rotation rotation, String ownerName,
                               ArrayList<BuildingBlock> blocks, boolean isCapitol) {
        super(building, level, originPos, rotation, ownerName, blocks, isCapitol);
    }

    @Override
    public boolean startProductionItem(ProductionItem prodItem) {
        // Guard: block rocket over-queue on the server
        if (prodItem instanceof RocketProd) {
            int stored = this.getCharges(ProduceRocketAbility.INSTANCE);
            int inQueue = 0;
            for (ActiveProduction ap : this.productionQueue) {
                if (ap.item instanceof RocketProd) {
                    inQueue++;
                }
            }
            int maxRockets = ProduceRocketAbility.getMaxRockets();
            if (stored + inQueue >= maxRockets) {
                return false; // blocked — queue is full
            }
        }
        return super.startProductionItem(prodItem);
    }
}
