package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ProduceRocketAbility extends Ability {

    public static final int MAX_ROCKETS = 2;

    public ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        this.maxCharges = MAX_ROCKETS;
    }

    // No button override = no HUD slot
}
