package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.network.ShieldActivateServerboundPacket;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class ShieldInterceptAbility extends Ability {

    // 0-second cooldown — the limitation is building damage, not time
    private static final int COOLDOWN = 0;
    private static final int ACTIVE_DURATION = 10 * 20;

    /** Fraction of the shield array's blocks to destroy on intercept (0.80 = 80%). */
    public static final float DAMAGE_FRACTION = 0.80f;

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, COOLDOWN, ShieldArrayBuilding.SHIELD_RADIUS, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        String title = I18n.get("abilities.ronrockets.shield_intercept");
        float healthPct = getHealthPercent(placement);
        boolean isReady = healthPct >= 1.0f;

        return new AbilityButton(
            title,
            ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
            hotkey,
            () -> isShieldActive(placement),
            () -> false,
            () -> isReady,
            () -> ShieldActivateServerboundPacket.send(placement.originPos),
            null,
            List.of(
                fcs(title, true),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip1", (int)(healthPct * 100))),
                isReady
                    ? fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip2"))
                    : fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip_damaged")),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip3", ShieldArrayBuilding.SHIELD_RADIUS)),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip5"))
            ),
            this,
            placement
        );
    }

    /** Returns the building's health as a 0..1 fraction based on blocks remaining. */
    public static float getHealthPercent(BuildingPlacement placement) {
        if (placement.getBlocksTotal() == 0) return 0;
        return (float) placement.getBlocksPlaced() / placement.getBlocksTotal();
    }

    /** Is the shield building fully repaired and ready to use? */
    public static boolean isReady(BuildingPlacement placement) {
        return getHealthPercent(placement) >= 1.0f;
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) {
            return;
        }

        // Can only use at full health
        if (!isReady(buildingUsing)) {
            return;
        }

        // Damage ~80% of the building's blocks
        int totalBlocks = buildingUsing.getBlocksTotal();
        int blocksToDestroy = (int) (totalBlocks * DAMAGE_FRACTION);
        if (blocksToDestroy > 0) {
            buildingUsing.destroyRandomBlocks(blocksToDestroy);
            if (buildingUsing.shouldBeDestroyed()) {
                com.solegendary.reignofnether.building.BuildingServerEvents.cancelBuilding(buildingUsing, buildingUsing.ownerName);
                return;
            }
        }

        this.setToMaxCooldown(buildingUsing);
        buildingUsing.updateButtons();
        spawnActivationParticles(level, buildingUsing.centrePos);
    }

    public static ShieldInterceptAbility getFrom(BuildingPlacement placement) {
        for (var ability : placement.getAbilities()) {
            if (ability instanceof ShieldInterceptAbility shieldAbility) {
                return shieldAbility;
            }
        }
        return null;
    }

    public boolean isShieldActive(BuildingPlacement placement) {
        return getCooldown(placement) > (cooldownMax - ACTIVE_DURATION);
    }

    public static void spawnInterceptParticles(ServerLevel level, BlockPos shieldPos, BlockPos targetPos) {
        level.sendParticles(
            ParticleTypes.END_ROD,
            targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5,
            60, 1.0, 1.0, 1.0, 0.15
        );
        level.sendParticles(
            ParticleTypes.ENCHANT,
            shieldPos.getX() + 0.5, shieldPos.getY() + 3.0, shieldPos.getZ() + 0.5,
            80, 2.0, 1.5, 2.0, 0.1
        );
        level.playSound(null, shieldPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.4f);
        level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    private static void spawnActivationParticles(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            for (int i = 0; i < 50; i++) {
                double dx = (level.random.nextDouble() - 0.5D) * 4.0D;
                double dy = level.random.nextDouble() * 2.0D;
                double dz = (level.random.nextDouble() - 0.5D) * 4.0D;
                level.addParticle(
                    ParticleTypes.ENCHANT,
                    pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D,
                    dx * 0.04D, dy * 0.04D, dz * 0.04D
                );
            }
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.sendParticles(
            ParticleTypes.ENCHANT,
            pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D,
            120, 2.0D, 2.0D, 2.0D, 0.1D
        );
        serverLevel.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.2f);
    }
}
