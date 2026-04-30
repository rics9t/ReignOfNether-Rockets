package com.rics.ronrockets.rocket;

import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ActiveProduction;
import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.StartProductionButton;
import com.solegendary.reignofnether.building.production.StopProductionButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class RocketProd extends ProductionItem {

    private static final Logger LOG = LogManager.getLogger("RonRockets");

    public static final ResourceCost COST = ResourceCost.Unit(0, 500, 1000, 120, 0);

    public RocketProd() {
        super(COST);

        this.onComplete = (level, placement) -> {
            int current = placement.getCharges(ProduceRocketAbility.INSTANCE);
            int max = ProduceRocketAbility.getMaxRockets();
            if (current < max) {
                placement.setCharges(ProduceRocketAbility.INSTANCE, current + 1);
            }
            placement.updateButtons();
        };
    }

    /**
     * Counts how many rockets are stored + currently in the production queue.
     */
    private static int getTotalRockets(ProductionPlacement placement) {
        int stored = placement.getCharges(ProduceRocketAbility.INSTANCE);
        int inQueue = 0;
        for (ActiveProduction ap : placement.productionQueue) {
            if (ap.item instanceof RocketProd) {
                inQueue++;
            }
        }
        return stored + inQueue;
    }

    /**
     * Server-side guard: treat the rocket as unaffordable when
     * stored + in-queue already equals the max limit.
     * This prevents the server from accepting over-queued productions.
     */
    @Override
    public boolean canAfford(Level level, String ownerName) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            for (BuildingPlacement b : com.solegendary.reignofnether.building.BuildingServerEvents.getBuildings()) {
                if (b instanceof ProductionPlacement pp && b.ownerName.equals(ownerName)
                        && b.getBuilding() instanceof com.rics.ronrockets.building.AbstractRocketSilo) {
                    if (getTotalRockets(pp) >= ProduceRocketAbility.getMaxRockets()) {
                        return false;
                    }
                }
            }
        }
        return super.canAfford(level, ownerName);
    }

    @Override
    public String getItemName() {
        return "Rocket";
    }

    @Override
    public StartProductionButton getStartButton(ProductionPlacement prodBuilding, Keybinding hotkey) {
        try {
            int storedRockets = prodBuilding.getCharges(ProduceRocketAbility.INSTANCE);
            int maxRockets = ProduceRocketAbility.getMaxRockets();
            int totalRockets = getTotalRockets(prodBuilding);
            String title = I18n.get("abilities.ronrockets.produce_rocket");

            boolean canQueue = totalRockets < maxRockets;

            return new StartProductionButton(
                title,
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> canQueue,
                List.of(
                    fcs(title, true),
                    fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip1", storedRockets, maxRockets)),
                    ResourceCosts.getFormattedCost(COST),
                    ResourceCosts.getFormattedPopAndTime(COST),
                    canQueue
                        ? fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip2"))
                        : fcs(I18n.get("abilities.ronrockets.produce_rocket.queue_full")),
                    fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip3", maxRockets))
                ),
                this
            );
        } catch (Exception e) {
            LOG.error("RocketProd.getStartButton() FAILED!", e);
            throw e;
        }
    }

    @Override
    public StopProductionButton getCancelButton(ProductionPlacement prodBuilding, boolean first) {
        return new StopProductionButton(
            "Rocket",
            ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
            prodBuilding,
            this,
            first
        );
    }
}
