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

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class LaunchRocketAbility extends Ability {

    private static final int LAUNCH_COOLDOWN = 100;

    public LaunchRocketAbility() {
        super(UnitAction.ATTACK_GROUND, LAUNCH_COOLDOWN, 9999, 8, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        return new AbilityButton(
                I18n.get("abilities.ronrockets.launch_rocket"),
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/launch_rocket.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK_GROUND,
                () -> false,
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK_GROUND),
                null,
                List.of(
                        FormattedCharSequence.forward(
                                I18n.get("abilities.ronrockets.launch_rocket"),
                                Style.EMPTY.withBold(true)
                        ),
                        FormattedCharSequence.forward(
                                I18n.get("tooltip.ronrockets.launch_damage"),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                I18n.get("tooltip.ronrockets.unit_damage"),
                                Style.EMPTY
                        )
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos targetBp) {
        if (level.isClientSide()) return;
        if (!(buildingUsing.getBuilding() instanceof AbstractRocketSilo)) return;

        // Find the ProduceRocketAbility instance on this placement
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

        // Spawn rocket entity
        RocketEntity rocket = new RocketEntity(
                RocketEntities.ROCKET.get(), serverLevel
        );
        rocket.setPos(
                buildingUsing.centrePos.getX() + 0.5,
                buildingUsing.centrePos.getY() + 5,
                buildingUsing.centrePos.getZ() + 0.5
        );
        rocket.setTarget(targetBp);
        rocket.setAttacker(buildingUsing.ownerName);

        serverLevel.addFreshEntity(rocket);

        // Consume charge
        buildingUsing.setCharges(
                produceAbility,
                buildingUsing.getCharges(produceAbility) - 1
        );

        // Launch cooldown
        this.setToMaxCooldown(buildingUsing);
    }
}
