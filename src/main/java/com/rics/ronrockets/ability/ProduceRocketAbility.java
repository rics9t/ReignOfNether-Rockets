package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.world.level.Level;

public class ProduceRocketAbility extends Ability {

    private static final int PRODUCTION_TIME = 2400; // 120 seconds

    public ProduceRocketAbility() {
        super(UnitAction.NONE, PRODUCTION_TIME, 0, 0, false);
        this.maxCharges = 2;
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, net.minecraft.core.BlockPos bp) {

        if (level.isClientSide()) return;

        // Do not produce if already full
        if (buildingUsing.getCharges(this) >= maxCharges) return;

        // Start cooldown
        buildingUsing.setCooldown(this, cooldownMax);
    }
}
