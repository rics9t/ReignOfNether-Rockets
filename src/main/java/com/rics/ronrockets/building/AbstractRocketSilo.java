package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.resources.ResourceCost;

public abstract class AbstractRocketSilo extends Building {

    public AbstractRocketSilo(String structureName) {
        super(structureName,
                ResourceCost.Building(1000, 800, 600, 0)
                false
        );

        this.name = "Rocket Silo";

        this.abilities.add(new ProduceRocketAbility());
        this.abilities.add(new LaunchRocketAbility());
    }
}
