package com.rics.ronrockets.ability;

import com.rics.ronrockets.rocket.RocketManager;
import com.rics.ronrockets.rocket.RocketStrike;
import com.rics.ronrockets.building.AbstractRocketSilo;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class LaunchRocketAbility extends Ability {

    private static final int LAUNCH_COOLDOWN = 100; // 5 seconds

    public LaunchRocketAbility() {
        super(UnitAction.ATTACK_GROUND, LAUNCH_COOLDOWN, 9999, 8, false);
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos targetBp) {

        if (level.isClientSide()) return;
        if (!(buildingUsing.getBuilding() instanceof AbstractRocketSilo)) return;

        if (!isOffCooldown(buildingUsing)) return;

        // Find produce ability to check charges
        Ability produceAbility = null;

        for (Ability ability : buildingUsing.getAbilities()) {
            if (ability instanceof ProduceRocketAbility) {
                produceAbility = ability;
                break;
            }
        }

        if (produceAbility == null) return;

        if (buildingUsing.getCharges(produceAbility) <= 0) return;

        ServerLevel serverLevel = (ServerLevel) level;

        long currentTick = serverLevel.getGameTime();
        double distance = Math.sqrt(buildingUsing.centrePos.distSqr(targetBp));
        long travelTime = (long)(distance * 2);

        RocketStrike strike = new RocketStrike(
                buildingUsing.ownerName,
                buildingUsing.centrePos,
                targetBp,
                currentTick + travelTime
        );

        RocketManager.registerStrike(strike);

        // Consume rocket
        buildingUsing.setCharges(produceAbility,
                buildingUsing.getCharges(produceAbility) - 1);

        // Set launch cooldown
        buildingUsing.setCooldown(this, cooldownMax);
    }
}
