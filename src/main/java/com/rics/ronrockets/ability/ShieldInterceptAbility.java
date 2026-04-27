package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    private static final int COOLDOWN = 30 * ResourceCost.TICKS_PER_SECOND;
    private static final int ACTIVE_DURATION = 10 * ResourceCost.TICKS_PER_SECOND;

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, COOLDOWN, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        return new AbilityButton(
                "Shield Intercept",
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/shield_intercept.png"),
                hotkey,
                () -> isShieldActive(placement),
                () -> false,
                () -> isOffCooldown(placement),
                () -> use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward("Activate Shield", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("Duration: 10s", Style.EMPTY),
                        FormattedCharSequence.forward("Cooldown: 30s", Style.EMPTY)
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) return;
        this.setToMaxCooldown(buildingUsing);
    }

    public boolean isShieldActive(BuildingPlacement placement) {
        return getCooldown(placement) > (cooldownMax - ACTIVE_DURATION);
    }
}
