package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.rics.ronrockets.rocket.RocketProd;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public abstract class AbstractRocketSilo extends ProductionBuilding {

    public AbstractRocketSilo(String structureName) {
        super(structureName, ResourceCost.Building(1000, 800, 600, 0), false);

        this.name = "Rocket Silo";

        // Production queue
        this.productions.add(new RocketProd(), Keybindings.keyQ);

        // Launch ability (visible)
        this.abilities.add(new LaunchRocketAbility(), Keybindings.keyW);
    }

    @Override
    public BuildingPlacement createBuildingPlacement(
            Level level,
            BlockPos pos,
            Rotation rotation,
            String ownerName) {

        BuildingPlacement placement =
                super.createBuildingPlacement(level, pos, rotation, ownerName);

        // ✅ Inject hidden storage ability AFTER creation
        placement.getAbilities().add(new ProduceRocketAbility());

        return placement;
    }
}
