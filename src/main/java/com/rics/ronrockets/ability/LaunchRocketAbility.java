package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.AbstractRocketSilo;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.entity.RocketEntity;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class LaunchRocketAbility extends Ability {

    private static final int COOLDOWN = 100;

    public LaunchRocketAbility() {
        super(UnitAction.ATTACK_GROUND, COOLDOWN, 9999, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        return new AbilityButton(
                "Launch Rocket",
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/launch_rocket.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK_GROUND,
                () -> {
                    for (var ability : placement.getAbilities()) {
                        if (ability instanceof ProduceRocketAbility produce) {
                            return placement.getCharges(produce) <= 0;
                        }
                    }
                    return true;
                },
                () -> placement.isAbilityOffCooldown(UnitAction.ATTACK_GROUND),
                () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK_GROUND),
                null,
                List.of(
                        FormattedCharSequence.forward("Launch Rocket", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("Click target location", Style.EMPTY)
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos targetBp) {

        if (level.isClientSide()) return;
        if (!(buildingUsing.getBuilding() instanceof AbstractRocketSilo)) return;

        Ability produceAbility = null;

        for (var ability : buildingUsing.getAbilities()) {
            if (ability instanceof ProduceRocketAbility) {
                produceAbility = ability;
                break;
            }
        }

        if (produceAbility == null) return;
        if (buildingUsing.getCharges(produceAbility) <= 0) return;

        ServerLevel serverLevel = (ServerLevel) level;

        RocketEntity rocket = new RocketEntity(
                RocketEntities.ROCKET.get(),
                serverLevel
        );

        rocket.setPos(
                buildingUsing.centrePos.getX() + 0.5,
                buildingUsing.centrePos.getY() + 5,
                buildingUsing.centrePos.getZ() + 0.5
        );

        rocket.setTarget(targetBp);
        rocket.setAttacker(buildingUsing.ownerName);

        serverLevel.addFreshEntity(rocket);

        buildingUsing.setCharges(
                produceAbility,
                buildingUsing.getCharges(produceAbility) - 1
        );

        this.setToMaxCooldown(buildingUsing);
    }
}
