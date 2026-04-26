package com.rics.ronrockets.building;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractRocketSilo extends Building {

    public AbstractRocketSilo(String structureName) {
        super(structureName,
                new ResourceCost(1000, 800, 600, 0),
                false
        );

        this.name = "Rocket Silo";
    }

    /**
     * Enforce only ONE silo per team
     */
    public static boolean teamAlreadyHasSilo(String ownerName) {

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (placement.getBuilding() instanceof AbstractRocketSilo
                    && placement.ownerName.equals(ownerName)
                    && placement.isBuilt) {
                return true;
            }
        }

        return false;
    }
}