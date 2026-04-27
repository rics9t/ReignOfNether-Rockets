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

// ✅ FIX 1: Make sure to extend AbstractRocketSilo so you inherit the abilities!
public class VillagerRocketSilo extends AbstractRocketSilo {

    public static final String STRUCTURE_NAME = "villager_rocket_silo";
    public static final ResourceCost COST = ResourceCost.Building(1000, 800, 600, 0);

    public VillagerRocketSilo() {
        // ✅ FIX 2: Pass the STRUCTURE_NAME to the superclass constructor
        super(STRUCTURE_NAME);

        this.portraitBlock = Blocks.IRON_BLOCK;
        this.icon = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "textures/block/iron_block.png"
        );

        this.startingBlockTypes.add(Blocks.IRON_BLOCK);
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGERS;
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

        // ✅ FIX 3: Wrap the entire restriction check in !level.isClientSide()
        if (!level.isClientSide()) {
            for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
                if (placement.getBuilding() instanceof AbstractRocketSilo // Use the abstract class here!
                        && placement.ownerName.equals(ownerName)
                        && placement.isBuilt) {

                    PlayerServerEvents.sendMessageToAllPlayers(
                            "You already have a Rocket Silo!",
                            false,
                            ownerName
                    );

                    return null; // cancel placement
                }
            }
        }

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
