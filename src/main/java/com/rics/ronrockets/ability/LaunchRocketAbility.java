package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.AbstractRocketSilo;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.entity.RocketEntity;
import com.rics.ronrockets.rocket.RocketManager;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class LaunchRocketAbility extends Ability {

    public LaunchRocketAbility() {
        super(UnitAction.ATTACK_GROUND, 0, 9999, 8, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        int stored = RocketManager.storedRockets.getOrDefault(placement.centrePos, 0);

        return new AbilityButton(
                "Launch Rocket",
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/launch_rocket.png"),
                hotkey,
                () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK_GROUND,
                () -> stored <= 0, // ✅ Locked if 0 rockets stored
                () -> true,
                () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK_GROUND),
                null,
                List.of(
                        FormattedCharSequence.forward("Launch Rocket", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("Click anywhere to strike.", Style.EMPTY)
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos targetBp) {
        if (level.isClientSide()) return;
        if (!(buildingUsing.getBuilding() instanceof AbstractRocketSilo)) return;

        int stored = RocketManager.storedRockets.getOrDefault(buildingUsing.centrePos, 0);
        if (stored <= 0) return;

        // ✅ Consume 1 Rocket
        RocketManager.storedRockets.put(buildingUsing.centrePos, stored - 1);

        ServerLevel serverLevel = (ServerLevel) level;
        RocketEntity rocket = new RocketEntity(RocketEntities.ROCKET.get(), serverLevel);
        rocket.setPos(buildingUsing.centrePos.getX() + 0.5, buildingUsing.centrePos.getY() + 5, buildingUsing.centrePos.getZ() + 0.5);
        rocket.setTarget(targetBp);
        rocket.setAttacker(buildingUsing.ownerName);
        serverLevel.addFreshEntity(rocket);
    }
}
