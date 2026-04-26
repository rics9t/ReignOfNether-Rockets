package com.rics.ronrockets.building;

import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlaceButton;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class PiglinRocketSilo extends AbstractRocketSilo {

    public PiglinRocketSilo() {
        super("piglin_rocket_silo");
        this.portraitBlock = Blocks.IRON_BLOCK;
    }

    @Override
    public Faction getFaction() {
        return Faction.PIGLINS;
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {
        return new BuildingPlaceButton(
                this.name,
                ResourceLocation.withDefaultNamespace("textures/block/iron_block.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == this,
                () -> false,
                () -> true,
                java.util.List.of(),
                this
        );
    }
}