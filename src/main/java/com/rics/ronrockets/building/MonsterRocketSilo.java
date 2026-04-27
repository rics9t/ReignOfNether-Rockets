package com.rics.ronrockets.building;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;
import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class MonsterRocketSilo extends Building {

    public static final String STRUCTURE_NAME = "monster_rocket_silo";
    public static final ResourceCost COST =
            ResourceCost.Building(1000, 800, 600, 0);

    public MonsterRocketSilo() {
        super(STRUCTURE_NAME, COST, false);

        this.name = "Rocket Silo";
        this.portraitBlock = Blocks.OBSIDIAN;
        this.icon = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "textures/block/obsidian.png"
        );

        this.startingBlockTypes.add(Blocks.OBSIDIAN);
    }

    @Override
    public Faction getFaction() {
        return Faction.MONSTERS;
    }

    @Override
    public ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocksFromNbt(
                this.structureName,
                level
        );
    }

    @Override
    public BuildingPlacement createBuildingPlacement(
            Level level,
            BlockPos pos,
            Rotation rotation,
            String ownerName
    ) {
        return new BuildingPlacement(
                this,
                level,
                pos,
                rotation,
                ownerName,
                getAbsoluteBlockData(
                        getRelativeBlockData(level),
                        level,
                        pos,
                        rotation
                ),
                false
        );
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {

        ResourceLocation key =
                ReignOfNetherRegistries.BUILDING.getKey(this);

        String name = I18n.get(
                "buildings.monsters." +
                        key.getNamespace() +
                        "." +
                        key.getPath()
        );

        return new BuildingPlaceButton(
                name,
                this.icon,
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace()
                        == RocketBuildings.MONSTER_SILO,
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
