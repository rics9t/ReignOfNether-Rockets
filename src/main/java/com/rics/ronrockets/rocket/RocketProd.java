package com.rics.ronrockets.rocket;

import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.StartProductionButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class RocketProd extends ProductionItem {

    public static final ResourceCost COST =
            ResourceCost.Unit(0, 500, 1000, 120, 0);

    public RocketProd() {
        super(COST);

        this.onComplete = (Level level, ProductionPlacement placement) -> {
            if (!level.isClientSide()) {

                placement.getAbilities().forEach(ability -> {
                    if (ability instanceof ProduceRocketAbility produce) {
                        int current = placement.getCharges(produce);
                        if (current < produce.maxCharges) {
                            placement.setCharges(produce, current + 1);
                        }
                    }
                });
            }
        };
    }

    @Override
    public String getItemName() {
        return "Rocket";
    }

    @Override
    public StartProductionButton getStartButton(ProductionPlacement placement, Keybinding hotkey) {

        return new StartProductionButton(
                "Rocket",
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> placement.getAbilities().stream()
                        .anyMatch(a -> a instanceof ProduceRocketAbility produce &&
                                placement.getCharges(produce) >= produce.maxCharges),
                List.of(
                        FormattedCharSequence.forward("Produce Rocket", Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(COST),
                        ResourceCosts.getFormattedPopAndTime(COST)
                ),
                this
        );
    }
}
