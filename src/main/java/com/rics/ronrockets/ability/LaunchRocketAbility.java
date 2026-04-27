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

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

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
                () -> placement.getAbilities().stream()
                        .noneMatch(a -> a instanceof ProduceRocketAbility produce &&
                                placement.getCharges(produce) > 0),
                () -> isOffCooldown(placement),
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

        for (var ability : buildingUsing.getAbilities()) {
            if (ability instanceof ProduceRocketAbility produce) {

                if (buildingUsing.getCharges(produce) <= 0) return;

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

                buildingUsing.setCharges(produce,
                        buildingUsing.getCharges(produce) - 1);

                this.setToMaxCooldown(buildingUsing);
                break;
            }
        }
    }
}
