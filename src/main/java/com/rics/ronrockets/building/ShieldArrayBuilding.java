package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlaceButton;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ShieldArrayBuilding extends Building {

    public static final int SHIELD_RADIUS = 64;

    public ShieldArrayBuilding(String structureName) {
        super(structureName,
                ResourceCost.Building(200, 300, 500, 0),
                false
        );

        this.name = "Shield Array";
        this.portraitBlock = Blocks.BEACON;

        // ✅ Ability must be added INSIDE constructor
        this.abilities.add(new ShieldInterceptAbility());
    }

    @Override
    public Faction getFaction() {
        return Faction.NONE;
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {
        return new BuildingPlaceButton(
                this.name,
                ResourceLocation.withDefaultNamespace("textures/block/beacon.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == this,
                () -> false,
                () -> true,
                List.of(),
                this
        );
    }
}
