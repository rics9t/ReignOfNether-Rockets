package com.rics.ronrockets.building;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlaceButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class VillagerRocketSilo extends com.solegendary.reignofnether.building.Building {

    public static final String STRUCTURE_NAME = "villager_rocket_silo";
    public static final ResourceCost COST = ResourceCost.Building(1000, 800, 600, 0);

    public VillagerRocketSilo() {
        super(STRUCTURE_NAME, COST, false);

        this.name = "Rocket Silo";
        this.portraitBlock = Blocks.IRON_BLOCK;
        this.icon = ResourceLocation.fromNamespaceAndPath(
                "minecraft", "textures/block/iron_block.png"
        );
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGERS;
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {

        ResourceLocation key = ReignOfNetherRegistries.BUILDING.getKey(this);

        String name = I18n.get(
                "buildings." +
                        getFaction().name().toLowerCase() +
                        "." +
                        key.getNamespace() +
                        "." +
                        key.getPath()
        );

        return new BuildingPlaceButton(
                name,
                this.icon,
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == RocketBuildings.VILLAGER_SILO,
                () -> false,
                () -> true,
                List.of(
                        fcs(name, true),
                        ResourceCosts.getFormattedCost(COST)
                ),
                this
        );
    }
}
