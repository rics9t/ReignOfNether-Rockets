package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlaceButton;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.buildings.placements.RangeIndicatorProductionPlacement;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class ShieldArrayBuilding extends ProductionBuilding {

    public static final String STRUCTURE_NAME = "shield_array";
    public static final ResourceCost COST = ResourceCost.Building(0, 150, 200, 0);
    public static final int SHIELD_RADIUS = 64;

    public ShieldArrayBuilding() {
        super(STRUCTURE_NAME, COST, false);
        this.name = "Shield Array";
        this.portraitBlock = Blocks.BEACON;
        this.icon = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/beacon.png");
        this.startingBlockTypes.add(Blocks.LODESTONE);

        this.abilities.add(new ShieldInterceptAbility(), Keybindings.keyQ);
    }

    @Override
    public Faction getFaction() { return Faction.NONE; }

    @Override
    public float getMeleeDamageMult() {
        return 2.0f; // very fragile — one rocket hit can nearly destroy it
    }

    @Override
    public BuildingPlacement createBuildingPlacement(Level level, BlockPos pos, Rotation rotation, String ownerName) {
        return new RangeIndicatorProductionPlacement(
            this, level, pos, rotation, ownerName,
            getAbsoluteBlockData(getRelativeBlockData(level), level, pos, rotation),
            false, SHIELD_RADIUS, true, false
        );
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {
        ResourceLocation key = ReignOfNetherRegistries.BUILDING.getKey(this);
        String name = I18n.get("buildings.none." + key.getNamespace() + "." + key.getPath());

        return new BuildingPlaceButton(
            name, this.icon, hotkey,
            () -> BuildingClientEvents.getBuildingToPlace() == RocketBuildings.SHIELD_ARRAY,
            () -> false, () -> true,
            List.of(
                FormattedCharSequence.forward(name, Style.EMPTY.withBold(true)),
                ResourceCosts.getFormattedCost(COST),
                FormattedCharSequence.forward("Intercept Radius: " + SHIELD_RADIUS, Style.EMPTY),
                FormattedCharSequence.forward("Fragile — intercept destroys 80% of building", Style.EMPTY),
                FormattedCharSequence.forward("Must be fully repaired to activate", Style.EMPTY)
            ), this
        );
    }
}
