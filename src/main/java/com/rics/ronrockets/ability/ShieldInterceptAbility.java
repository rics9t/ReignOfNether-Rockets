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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class ShieldInterceptAbility extends Ability {

    private static final Logger LOG = LogManager.getLogger("RonRockets/Shield");

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

        return new AbilityButton(
            title,
            ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
            hotkey,
            () -> isShieldActive(placement),
            () -> false,
            () -> isReady(placement),
            () -> ShieldActivateServerboundPacket.send(placement.originPos),
            null,
            List.of(
                fcs(title, true),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip1",
                    (int)(getHealthPercent(placement) * 100))),
                isReady(placement)
                    ? fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip2"))
                    : fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip_damaged")),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip3", ShieldArrayBuilding.SHIELD_RADIUS)),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip5"))
            ),
            this,
            placement
        );
    }

    /** Returns the building's health as a 0..1 fraction.
     *  Server: uses placedBlockPosSet.size() / totalBlocks — accurate.
     *  Client: uses serverBlocksPlaced / totalBlocks — may lag, so if
     *  isBuilt is true but blocksPlaced is suspiciously low, we report
     *  1.0 to avoid showing a misleading "1%" for a healthy building. */
    public static float getHealthPercent(BuildingPlacement placement) {
        int blocksPlaced = placement.getBlocksPlaced();
        int total = placement.getBlocksTotal();
        boolean isBuilt = placement.isBuilt;
        boolean isClient = placement.level.isClientSide;

        LOG.debug("getHealthPercent: isClient={}, isBuilt={}, blocksPlaced={}, total={}, pct={}",
            isClient, isBuilt, blocksPlaced, total,
            total > 0 ? String.format("%.2f", (float)blocksPlaced / total) : "N/A");

        if (total <= 0) return 0;
        if (!isBuilt) return 0;

        float pct = (float) blocksPlaced / total;

        // Client sync lag: isBuilt=true means building was 100% at some point.
        // If blocksPlaced hasn't synced yet (very low value), trust isBuilt.
        // However, after intercept (80% damage), pct would be ~0.2 — that's
        // legitimate damage, not sync lag. We distinguish by checking if
        // blocksPlaced is extremely low (< 10% total) which is almost
        // certainly sync lag (serverBlocksPlaced defaults to 1).
        if (isClient && pct < 0.10f && isBuilt) {
            LOG.debug("getHealthPercent: client sync lag detected (pct={}), returning 1.0", pct);
            return 1.0f;
        }

        return Math.min(1.0f, pct);
    }

    /** Is the shield building fully repaired and ready to use?
     *  Server: precise check of blocksPlaced >= totalBlocks.
     *  Client: uses isBuilt + getHealthPercent (which handles sync lag).
     *  The server-side use() validates authoritatively before acting. */
    public static boolean isReady(BuildingPlacement placement) {
        boolean isBuilt = placement.isBuilt;
        float healthPct = getHealthPercent(placement);

        if (!isBuilt) return false;

        boolean ready = healthPct >= 0.99f;
        LOG.debug("isReady: isClient={}, isBuilt={}, healthPct={}, ready={}",
            placement.level.isClientSide, isBuilt, healthPct, ready);
        return ready;
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) {
            return;
        }

        float healthPct = getHealthPercent(buildingUsing);
        int blocksPlaced = buildingUsing.getBlocksPlaced();
        int totalBlocks = buildingUsing.getBlocksTotal();

        LOG.info("Shield use() called: healthPct={}, blocksPlaced={}, total={}, isBuilt={}",
            healthPct, blocksPlaced, totalBlocks, buildingUsing.isBuilt);

        // Can only use at full health
        if (!isReady(buildingUsing)) {
            LOG.warn("Shield use() BLOCKED — not ready! healthPct={}", healthPct);
            return;
        }

        // Damage ~80% of the building's blocks
        int blocksToDestroy = (int) (totalBlocks * DAMAGE_FRACTION);
        LOG.info("Shield use() ACTIVATED — destroying {} of {} blocks", blocksToDestroy, totalBlocks);
        if (blocksToDestroy > 0) {
            buildingUsing.destroyRandomBlocks(blocksToDestroy);
            if (buildingUsing.shouldBeDestroyed()) {
                LOG.warn("Shield destroyed after intercept — too few blocks remaining");
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
