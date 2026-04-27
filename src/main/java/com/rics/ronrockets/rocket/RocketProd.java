package com.rics.ronrockets.rocket;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ProductionItem;
import com.solegendary.reignofnether.building.production.StartProductionButton;
import com.solegendary.reignofnether.building.production.StopProductionButton;
import com.solegendary.reignofnether.hud.buttons.UnitSpawnButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RocketProd extends ProductionItem {

    public final static String itemName = "Rocket";
    // ✅ FIXED: Using the public static factory method instead of the private constructor
    public final static ResourceCost cost = ResourceCost.Unit(0, 500, 1000, 120, 0);

    public RocketProd() {
        super(cost);
        this.onComplete = (Level level, ProductionPlacement placement) -> {
            if (!level.isClientSide()) {
                RocketManager.finishRocketProduction(placement.centrePos);
            }
        };
    }

    // ✅ FIXED: Removed @Override tags so it perfectly matches the base class signatures
    public String getItemName() { return itemName; }

    public UnitSpawnButton getPlaceButton() {
        return new UnitSpawnButton(
                itemName,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                List.of(FormattedCharSequence.forward("Rocket", Style.EMPTY))
        );
    }

    public StartProductionButton getStartButton(ProductionPlacement prodBuilding, Keybinding hotkey) {
        List<FormattedCharSequence> tooltips = new ArrayList<>(List.of(
                FormattedCharSequence.forward("Produce Rocket", Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(cost),
                ResourceCosts.getFormattedPopAndTime(cost),
                FormattedCharSequence.forward("Cooldown after creation: 3 minutes", Style.EMPTY)
        ));

        return new StartProductionButton(
                itemName,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> {
                    int stored = RocketManager.storedRockets.getOrDefault(prodBuilding.centrePos, 0);
                    int cool = RocketManager.cooldownTicks.getOrDefault(prodBuilding.centrePos, 0);
                    return stored >= 2 || cool > 0;
                },
                tooltips,
                this
        );
    }

    public StopProductionButton getCancelButton(ProductionPlacement prodBuilding, boolean first) {
        return new StopProductionButton(
                itemName,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                prodBuilding,
                this,
                first
        );
    }
}
