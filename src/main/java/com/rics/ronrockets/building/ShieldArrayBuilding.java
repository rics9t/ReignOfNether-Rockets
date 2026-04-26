package com.rics.ronrockets.building;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.resources.ResourceCost;

public class ShieldArrayBuilding extends Building {

    public static final int SHIELD_RADIUS = 64;

    public ShieldArrayBuilding(String structureName, Faction faction) {
        super(structureName,
                new ResourceCost(200, 300, 500, 0, 0)
                false
        );

        this.name = "Shield Array";
        this.portraitBlock = net.minecraft.world.level.block.Blocks.BEACON;
    }
    
    public abstract BuildingPlaceButton getBuildButton(Keybinding hotkey);
    
    @Override
    public Faction getFaction() {
        return Faction.NONE;
    }
}