package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsConfig;
import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.network.ShieldActivateServerboundPacket;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
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

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, RonRocketsConfig.getShieldCooldownSec() * 20, ShieldArrayBuilding.SHIELD_RADIUS, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        // Keep cooldownMax in sync with config
        int cooldownSec = RonRocketsConfig.getShieldCooldownSec();
        this.cooldownMax = cooldownSec * 20;

        String title = I18n.get("abilities.ronrockets.shield_intercept");
        return new AbilityButton(title,
            ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
            hotkey,
            () -> isShieldActive(placement),
            () -> false,
            () -> isReady(placement),
            () -> ShieldActivateServerboundPacket.send(placement.originPos),
            null,
            List.of(
                fcs(title, true),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip1", (int)(getHealthPercent(placement) * 100))),
                isReady(placement)
                    ? fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip2"))
                    : fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip_damaged")),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip3", ShieldArrayBuilding.SHIELD_RADIUS)),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip5", getActiveDurationTicks() / 20)),
                fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip6",
                    (int) RonRocketsConfig.getShieldIronCost(), cooldownSec))
            ),
            this,
            placement
        );
    }

    public static float getHealthPercent(BuildingPlacement placement) {
        if (placement.getBlocksTotal() <= 0 || !placement.isBuilt) return 0;
        float pct = (float) placement.getBlocksPlaced() / placement.getBlocksTotal();
        if (placement.level.isClientSide() && pct < 0.10f && placement.isBuilt) return 1.0f;
        return Math.min(1.0f, pct);
    }

    public static boolean isReady(BuildingPlacement placement) {
        return placement.isBuilt && getHealthPercent(placement) >= 0.99f;
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) return;
        if (!isReady(buildingUsing)) {
            LOG.warn("Shield use() BLOCKED — not ready!");
            return;
        }
        if (getCooldown(buildingUsing) > 0) {
            LOG.warn("Shield use() BLOCKED — on cooldown");
            return;
        }

        // Deduct iron cost
        int ironCost = (int) RonRocketsConfig.getShieldIronCost();
        if (ironCost > 0) {
            if (!ResourcesServerEvents.canAfford(buildingUsing.ownerName, ResourceName.ORE, ironCost)) {
                LOG.warn("Shield use() BLOCKED — insufficient iron (need {})", ironCost);
                return;
            }
            ResourcesServerEvents.addSubtractResources(new Resources(buildingUsing.ownerName, 0, 0, -ironCost));
            LOG.info("Shield activated — paid {} iron", ironCost);
        }

        // Damage is deferred — applied by ShieldVisualTickHandler after the active window ends
        this.cooldownMax = RonRocketsConfig.getShieldCooldownSec() * 20;
        this.setToMaxCooldown(buildingUsing);
        buildingUsing.updateButtons();
        spawnActivationParticles(level, buildingUsing.centrePos);
        LOG.info("Shield ACTIVATED — damage deferred until active window ends");
    }

    public static ShieldInterceptAbility getFrom(BuildingPlacement placement) {
        for (var ability : placement.getAbilities()) {
            if (ability instanceof ShieldInterceptAbility) return (ShieldInterceptAbility) ability;
        }
        return null;
    }

    public boolean isShieldActive(BuildingPlacement placement) {
        int cooldown = (int) getCooldown(placement);
        int activeDuration = getActiveDurationTicks();
        int max = (int) this.cooldownMax;
        // Cooldown starts at cooldownMax and ticks DOWN.
        // Shield is active during the first `activeDuration` ticks after activation.
        return cooldown > (max - activeDuration) && cooldown <= max;
    }

    public int getActiveDurationTicks() {
        return RonRocketsConfig.getShieldActiveDurationTicks();
    }

    public static void spawnInterceptParticles(ServerLevel level, BlockPos shieldPos, BlockPos interceptPos) {
        level.sendParticles(ParticleTypes.END_ROD,
            interceptPos.getX() + 0.5, interceptPos.getY() + 1.0, interceptPos.getZ() + 0.5,
            60, 1.0, 1.0, 1.0, 0.15);
        level.sendParticles(ParticleTypes.ENCHANT,
            shieldPos.getX() + 0.5, shieldPos.getY() + 3.0, shieldPos.getZ() + 0.5,
            80, 2.0, 1.5, 2.0, 0.1);
        level.playSound(null, shieldPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.4f);
        level.playSound(null, interceptPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    private static void spawnActivationParticles(Level level, BlockPos pos) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 3.0D;
        double cz = pos.getZ() + 0.5D;

        if (level.isClientSide()) {
            // Enchant particles
            for (int i = 0; i < 50; i++) {
                double dx = (level.random.nextDouble() - 0.5D) * 4.0D;
                double dy = level.random.nextDouble() * 2.0D;
                double dz = (level.random.nextDouble() - 0.5D) * 4.0D;
                level.addParticle(ParticleTypes.ENCHANT, cx, cy, cz,
                    dx * 0.04D, dy * 0.04D, dz * 0.04D);
            }
            // Smoke ring
            for (int i = 0; i < 20; i++) {
                double dx = (level.random.nextDouble() - 0.5D) * 3.0D;
                double dz = (level.random.nextDouble() - 0.5D) * 3.0D;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx + dx, cy - 1.0D, cz + dz,
                    dx * 0.02D, 0.08D, dz * 0.02D);
            }
            // Flame burst
            for (int i = 0; i < 15; i++) {
                double dx = (level.random.nextDouble() - 0.5D) * 2.5D;
                double dz = (level.random.nextDouble() - 0.5D) * 2.5D;
                level.addParticle(ParticleTypes.FLAME,
                    cx + dx, cy - 0.5D, cz + dz,
                    dx * 0.03D, 0.1D, dz * 0.03D);
            }
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        // Enchant dome
        serverLevel.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz,
            120, 2.0D, 2.0D, 2.0D, 0.1D);
        // Campfire smoke ring
        serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy - 1.0D, cz,
            30, 2.5D, 0.5D, 2.5D, 0.02D);
        // Flame burst
        serverLevel.sendParticles(ParticleTypes.FLAME, cx, cy - 0.5D, cz,
            20, 2.0D, 1.0D, 2.0D, 0.04D);
        // Sounds
        serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
    }
}
