package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsConfig;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.UnitAction;

public class ProduceRocketAbility extends Ability {

    // Use maxCharges=0 so the Ability.setToMaxCooldown() never auto-decrements charges.
    // We manage charges manually via RocketProd.onComplete and LaunchRocketAbility.use().
    public static final ProduceRocketAbility INSTANCE = new ProduceRocketAbility();

    private ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        this.maxCharges = 0;
    }

    /** Returns the configured rocket storage limit */
    public static int getMaxRockets() {
        return RonRocketsConfig.getRocketStorageLimit();
    }
}
