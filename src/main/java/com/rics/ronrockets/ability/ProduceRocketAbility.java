package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsConfig;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ProduceRocketAbility extends Ability {

    // Single shared instance used as the key for BuildingPlacement charges
    public static final ProduceRocketAbility INSTANCE = new ProduceRocketAbility();

    private ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        // maxCharges is set from config at construction; can be overridden at runtime
        this.maxCharges = RonRocketsConfig.getRocketStorageLimit();
    }

    /** Returns the current configured storage limit */
    public static int getMaxRockets() {
        return RonRocketsConfig.getRocketStorageLimit();
    }
}
