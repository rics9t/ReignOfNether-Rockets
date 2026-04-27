package com.rics.ronrockets.building;

import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.building.BuildingServerEvents;

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

public class PiglinRocketSilo extends Building {

    public static final String STRUCTURE_NAME = "piglin_rocket_silo";
    public static final ResourceCost COST =
            ResourceCost.Building(1000, 800, 600, 0);

    public PiglinRocketSilo() {
        super(STRUCTURE_NAME, COST, false);

        this.name = "Rocket Silo";
        this.portraitBlock = Blocks.GILDED_BLACKSTONE;
        this.icon = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "textures/block/gilded_blackstone.png"
        );

        this.startingBlockTypes.add(Blocks.GILDED_BLACKSTONE);
    }

    @Override
    public Faction getFaction() {
        return Faction.PIGLINS;
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
    public boolean canAfford(String ownerName) {

        for (BuildingPlacement placement :
                BuildingServerEvents.getBuildings()) {

            if (placement.getBuilding() instanceof PiglinRocketSilo
                    && placement.ownerName.equals(ownerName)
                    && placement.isBuilt) {

                PlayerServerEvents.sendMessageToAllPlayers(
                        "You already have a Rocket Silo!",
                        false,
                        ownerName
                );

                return false;
            }
        }

        return super.canAfford(ownerName);
    }

    @Override
    public BuildingPlaceButton getBuildButton(Keybinding hotkey) {

        ResourceLocation key =
                ReignOfNetherRegistries.BUILDING.getKey(this);

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
                () -> BuildingClientEvents.getBuildingToPlace() == this,
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
