package com.rics.ronrockets.ability;

import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    private static final int COOLDOWN = 30 * ResourceCost.TICKS_PER_SECOND;
    private static final int ACTIVE_DURATION = 10 * ResourceCost.TICKS_PER_SECOND;
    private static final int ACTIVATION_COST = 250;

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, COOLDOWN, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        return new AbilityButton(
                I18n.get("abilities.ronrockets.shield_intercept"),
                ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/icons/shield_intercept.png"),
                hotkey,
                () -> isShieldActive(placement),
                () -> ShieldEnergyManager.getEnergy(placement) < ACTIVATION_COST,
                () -> isOffCooldown(placement),
                () -> use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward(
                                I18n.get("abilities.ronrockets.shield_intercept"),
                                Style.EMPTY.withBold(true)
                        ),
                        FormattedCharSequence.forward(
                                I18n.get("tooltip.ronrockets.shield_energy",
                                        ShieldEnergyManager.getEnergy(placement)),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                I18n.get("tooltip.ronrockets.shield_duration"),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                I18n.get("tooltip.ronrockets.shield_cooldown"),
                                Style.EMPTY
                        )
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {

        if (level.isClientSide()) return;

        if (!ShieldEnergyManager.consumeEnergy(buildingUsing, ACTIVATION_COST))
            return;

        this.setToMaxCooldown(buildingUsing);

        // Activation burst
        ((ServerLevel) level).sendParticles(
                ParticleTypes.ENCHANT,
                buildingUsing.centrePos.getX() + 0.5,
                buildingUsing.centrePos.getY() + 3,
                buildingUsing.centrePos.getZ() + 0.5,
                120,
                2, 2, 2,
                0.1
        );
    }

    public boolean isShieldActive(BuildingPlacement placement) {
        return getCooldown(placement) > (cooldownMax - ACTIVE_DURATION);
    }

    public int getActiveDuration() {
        return ACTIVE_DURATION;
    }
}
