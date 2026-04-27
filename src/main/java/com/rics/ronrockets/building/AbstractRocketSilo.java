package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.rocket.RocketProduction;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;

public abstract class AbstractRocketSilo extends ProductionBuilding {

    public AbstractRocketSilo(String structureName) {
        super(structureName, ResourceCost.Building(1000, 800, 600, 0), false);
        this.name = "Rocket Silo";

        // Q = production (now visible again)
        this.productions.add(RocketProduction.ROCKET_PROD, Keybindings.keyQ);

        // W = launch
        this.abilities.add(new LaunchRocketAbility(), Keybindings.keyW);
    }

    @Override
    public float getMeleeDamageMult() {
        return 0.6f;
    }
}
