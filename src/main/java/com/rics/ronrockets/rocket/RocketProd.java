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
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RocketProd extends ProductionItem {

    public static final ResourceCost COST = ResourceCost.Unit(0, 500, 1000, 120, 0);

    public RocketProd() {
        super(COST);

        this.onComplete = (level, placement) -> {
            int current = placement.getCharges(ProduceRocketAbility.INSTANCE);
            int max = ProduceRocketAbility.MAX_ROCKETS;
            if (current < max) {
                placement.setCharges(ProduceRocketAbility.INSTANCE, current + 1);
            }
        };
    }

    @Override
    public String getItemName() {
        return "Rocket";
    }

    @Override
    public StartProductionButton getStartButton(ProductionPlacement prodBuilding, Keybinding hotkey) {
        List<FormattedCharSequence> tooltips = new ArrayList<>();
        tooltips.add(FormattedCharSequence.forward(
                I18n.get("abilities.ronrockets.produce_rocket"),
                Style.EMPTY.withBold(true)
        ));

        FormattedCharSequence costText = ResourceCosts.getFormattedCost(COST);
        if (costText != null) {
            tooltips.add(costText);
        }

        FormattedCharSequence popTimeText = ResourceCosts.getFormattedPopAndTime(COST);
        if (popTimeText != null) {
            tooltips.add(popTimeText);
        }

        return new StartProductionButton(
                I18n.get("abilities.ronrockets.produce_rocket"),
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> prodBuilding.getCharges(ProduceRocketAbility.INSTANCE)
                        >= ProduceRocketAbility.MAX_ROCKETS,
                tooltips,
                this
        );
    }

    @Override
    public StopProductionButton getCancelButton(ProductionPlacement prodBuilding, boolean first) {
        return new StopProductionButton(
                I18n.get("abilities.ronrockets.produce_rocket"),
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
                prodBuilding,
                this,
                first
        );
    }
}
