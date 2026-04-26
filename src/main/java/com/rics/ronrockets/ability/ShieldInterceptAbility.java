package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ShieldInterceptAbility extends Ability {

    private static final int COOLDOWN = 600; // 30 seconds

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, COOLDOWN, 0, 0, false);
    }
}
