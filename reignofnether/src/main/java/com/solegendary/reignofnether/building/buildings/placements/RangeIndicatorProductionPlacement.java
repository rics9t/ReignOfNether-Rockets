package com.solegendary.reignofnether.building.buildings.placements;

import com.solegendary.reignofnether.blocks.BlockClientEvents;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.NightSource;
import com.solegendary.reignofnether.building.RangeIndicator;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RangeIndicatorProductionPlacement extends ProductionPlacement implements RangeIndicator {
    int range;
    boolean showOnlyWhenSelected, checkUpgraded;
    private final Set<BlockPos> borderBps = new HashSet<>();
    public RangeIndicatorProductionPlacement(Building building, Level level, BlockPos originPos, Rotation rotation,
                                             String ownerName, ArrayList<BuildingBlock> blocks, boolean isCapitol,
                                             int range, boolean showOnlyWhenSelected, boolean checkUpgraded) {
        super(building, level, originPos, rotation, ownerName, blocks, isCapitol);
        this.range = range;
        this.showOnlyWhenSelected = showOnlyWhenSelected;
        this.checkUpgraded = checkUpgraded;
        updateHighlightBps();
    }

    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAgeAfterBuilt > 0 && tickAgeAfterBuilt % 100 == 0)
            updateHighlightBps();
    }

    public int getRange() {
        return ((!checkUpgraded || getUpgradeLevel() > 0) && isBuilt) ? range : 0;
    }

    @Override
    public void updateHighlightBps() {
        if (!level.isClientSide())
            return;
        this.borderBps.clear();
        this.borderBps.addAll(MiscUtil.getRangeIndicatorCircleBlocks(centrePos,
                range - BlockClientEvents.VISIBLE_BORDER_ADJ, level, this instanceof NightSource));
    }

    @Override
    public Set<BlockPos> getHighlightBps() {
        return borderBps;
    }

    @Override
    public boolean showOnlyWhenSelected() {
        return showOnlyWhenSelected;
    }
}
