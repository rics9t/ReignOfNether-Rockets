package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.rics.ronrockets.rocket.RocketProd;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;

public abstract class AbstractRocketSilo extends ProductionBuilding {

    public AbstractRocketSilo(String structureName) {
        super(structureName, ResourceCost.Building(1000, 800, 600, 0), false);

        this.name = "Rocket Silo";

        // ✅ Production queue
        this.productions.add(new RocketProd(), Keybindings.keyQ);

        // ✅ Storage ability FIRST (hidden)
        this.abilities.add(new ProduceRocketAbility());

        // ✅ Launch ability SECOND (visible)
        this.abilities.add(new LaunchRocketAbility(), Keybindings.keyW);
    }
}
