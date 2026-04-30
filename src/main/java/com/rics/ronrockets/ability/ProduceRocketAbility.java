package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsConfig;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ProduceRocketAbility extends Ability {

    public static final int DEFAULT_MAX_ROCKETS = 2;

    // Single shared instance used as the key for BuildingPlacement charges
    public static final ProduceRocketAbility INSTANCE = new ProduceRocketAbility();

    private ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        this.maxCharges = DEFAULT_MAX_ROCKETS;
    }

    /** Returns the configured rocket storage limit */
    public static int getMaxRockets() {
        return RonRocketsConfig.getRocketStorageLimit();
    }
}
