package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.rocket.RocketProduction;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.resources.ResourceCost;

// ✅ Extended ProductionBuilding so it gets the native Queue UI!
public abstract class AbstractRocketSilo extends ProductionBuilding {

    public AbstractRocketSilo(String structureName) {
        super(structureName, ResourceCost.Building(1000, 800, 600, 0), false);
        this.name = "Rocket Silo";

        this.abilities.add(new LaunchRocketAbility());
        
        // ✅ Hooks your custom Rocket Item into Reign of Nether's build queue
        this.productions.add(RocketProduction.ROCKET_PROD, Keybindings.keyQ);
    }

    // ✅ Multiplies all damage by 4, drastically reducing its effective ~300 block health down to ~75 equivalent health
    @Override
    public float getMeleeDamageMult() { return 0.6f; }
}
