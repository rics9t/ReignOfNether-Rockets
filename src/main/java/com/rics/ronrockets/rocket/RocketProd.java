package com.rics.ronrockets.rocket;

import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.StartProductionButton;
import com.solegendary.reignofnether.building.production.StopProductionButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class RocketProd extends ProductionItem {

    public static final ResourceCost COST = ResourceCost.Unit(0, 500, 1000, 120, 0);

    public RocketProd() {
        super(COST);

        this.onComplete = (level, placement) -> {
            // Run on BOTH sides so UI updates immediately.
            // Server remains authoritative.
            int current = placement.getCharges(ProduceRocketAbility.INSTANCE);
            int max = ProduceRocketAbility.INSTANCE.maxCharges;
            if (current < max) {
                placement.setCharges(ProduceRocketAbility.INSTANCE, current + 1);
            }
            placement.updateButtons();
        };
    }

    @Override
    public String getItemName() {
        return "Rocket";
    }

    @Override
    public StartProductionButton getStartButton(ProductionPlacement prodBuilding, Keybinding hotkey) {
        int storedRockets = prodBuilding.getCharges(ProduceRocketAbility.INSTANCE);
        int maxRockets = ProduceRocketAbility.INSTANCE.maxCharges;
        String title = I18n.get("abilities.ronrockets.produce_rocket");

        return new StartProductionButton(
                title,
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> storedRockets < maxRockets,
                List.of(
                        fcs(title, true),
                        fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip1", storedRockets, maxRockets)),
                        ResourceCosts.getFormattedCost(COST),
                        ResourceCosts.getFormattedPopAndTime(COST),
                        fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip2")),
                        fcs(I18n.get("abilities.ronrockets.produce_rocket.tooltip3", maxRockets))
                ),
                this
        );
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
