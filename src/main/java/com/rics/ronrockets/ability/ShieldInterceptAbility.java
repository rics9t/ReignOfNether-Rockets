package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    private static final int COOLDOWN = 30 * ResourceCost.TICKS_PER_SECOND;       // 30 сек
    public static final int ACTIVE_DURATION = 10 * ResourceCost.TICKS_PER_SECOND;  // 10 сек
    private static final int ACTIVATION_COST = 250;

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, COOLDOWN, 0, 0, false);
    }

    /**
     * Проверяет, активен ли щит для данного здания.
     * Щит активен, если кулдаун > (макс_кулдаун - длительность_активации).
     */
    public static boolean isShieldActive(BuildingPlacement placement) {
        // Предполагаем что ability хранит cooldown per-building
        // Активен в первые ACTIVE_DURATION тиков после использования
        ShieldInterceptAbility ability = getAbilityFromBuilding(placement);
        if (ability == null) return false;

        int currentCooldown = ability.getCooldown(placement);
        int maxCooldown = COOLDOWN;
        return currentCooldown > (maxCooldown - ACTIVE_DURATION);
    }

    private static ShieldInterceptAbility getAbilityFromBuilding(BuildingPlacement placement) {
        if (placement == null || placement.getBuilding() == null) return null;
        return placement.getBuilding().getAbilities().stream()
                .filter(a -> a instanceof ShieldInterceptAbility)
                .map(a -> (ShieldInterceptAbility) a)
                .findFirst()
                .orElse(null);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        return new AbilityButton(
                "Shield Intercept",
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
                hotkey,
                () -> isShieldActive(placement),
                () -> ShieldEnergyManager.getEnergy(placement) < ACTIVATION_COST,
                () -> isOffCooldown(placement),
                () -> use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward("Activate Shield", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward(
                                "Energy: " + ShieldEnergyManager.getEnergy(placement) + " / 1000",
                                Style.EMPTY),
                        FormattedCharSequence.forward("Cost: " + ACTIVATION_COST + " energy", Style.EMPTY),
                        FormattedCharSequence.forward("Duration: 10s | Cooldown: 30s", Style.EMPTY)
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

        // Визуальный всплеск активации
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos centre = buildingUsing.centrePos;

        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                centre.getX() + 0.5, centre.getY() + 2.0, centre.getZ() + 0.5,
                120, 4.0, 4.0, 4.0, 0.5);

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                centre.getX() + 0.5, centre.getY() + 3.0, centre.getZ() + 0.5,
                60, 3.0, 3.0, 3.0, 0.2);
    }
}
