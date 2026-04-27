package com.rics.ronrockets.building;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlaceButton;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class ShieldArrayBuilding extends Building {

    public static final String STRUCTURE_NAME = "shield_array";
    public static final ResourceCost COST = ResourceCost.Building(200, 300, 500, 0);

    public static final int SHIELD_RADIUS = 64;

    public ShieldArrayBuilding() {
        super(STRUCTURE_NAME, COST, false);

        this.name = "Shield Array";
        this.portraitBlock = Blocks.BEACON;
        this.icon = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "textures/block/beacon.png"
        );
    }

    @Override
    public Faction getFaction() {
        return Faction.NONE; // Will be registered to factions manually
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {

        ResourceLocation key = ReignOfNetherRegistries.BUILDING.getKey(this);

        String name = I18n.get(
                "buildings." +
                        "ronrockets" + "." +
                        key.getNamespace() +
                        "." +
                        key.getPath()
        );

        return new BuildingPlaceButton(
                name,
                this.icon,
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == this,
                () -> false,
                () -> true,
                List.of(
                        fcs(name, true),
                        ResourceCosts.getFormattedCost(COST),
                        fcs(""),
                        fcs("Intercept Radius: 64"),
                        fcs("Activation Cost: 150 Ore"),
                        fcs("Cooldown: 30s")
                ),
                this
        );
    }
}
