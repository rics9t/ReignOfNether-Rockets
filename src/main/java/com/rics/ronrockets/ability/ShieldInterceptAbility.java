package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsConfig;
import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.network.ShieldActivateServerboundPacket;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceCosts;
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
    super(UnitAction.NONE, 0, ShieldArrayBuilding.SHIELD_RADIUS, 0, false);
}

@Override
public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
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
            fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip5", RonRocketsConfig.getShieldActiveDurationTicks() / 20)),
            fcs(I18n.get("abilities.ronrockets.shield_intercept.tooltip6", (int)RonRocketsConfig.getShieldIronCost(), RonRocketsConfig.getShieldCooldownSec()))
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
    // Check if on cooldown (from config)
    int cooldownSec = RonRocketsConfig.getShieldCooldownSec();
    long currentCooldown = getCooldown(buildingUsing);
    if (currentCooldown > 0 && currentCooldown <= cooldownSec * 20) {
        LOG.warn("Shield use() BLOCKED — on cooldown ({} ticks left)", currentCooldown);
        return;
    }
    // Deduct iron cost
    double ironCost = RonRocketsConfig.getShieldIronCost();
    if (ironCost > 0) {
        var cost = ResourceCosts.of(0, ironCost, 0, 0);
        if (!buildingUsing.ownerResources.hasResources(cost)) {
            LOG.warn("Shield use() BLOCKED — insufficient iron (need {}, have {})", ironCost, buildingUsing.ownerResources.iron);
            return;
        }
        buildingUsing.ownerResources.removeResources(cost);
        LOG.info("Shield activated — paid {} iron", ironCost);
    }
    float damageFraction = RonRocketsConfig.getShieldDamageFraction();
    int blocksToDestroy = (int) (buildingUsing.getBlocksTotal() * damageFraction);
    LOG.info("Shield ACTIVATED — destroying {} blocks ({}%)", blocksToDestroy, (int)(damageFraction * 100));
    if (blocksToDestroy > 0) {
        buildingUsing.destroyRandomBlocks(blocksToDestroy);
        if (buildingUsing.shouldBeDestroyed()) {
            LOG.warn("Shield destroyed after intercept");
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
        if (ability instanceof ShieldInterceptAbility) return (ShieldInterceptAbility) ability;
    }
    return null;
}

public boolean isShieldActive(BuildingPlacement placement) {
    int cooldown = (int) getCooldown(placement);
    int activeDuration = RonRocketsConfig.getShieldActiveDurationTicks();
    return cooldown > 0 && cooldown <= activeDuration;
}

public int getActiveDurationTicks() {
    return RonRocketsConfig.getShieldActiveDurationTicks();
}

public static void spawnInterceptParticles(ServerLevel level, BlockPos shieldPos, BlockPos targetPos) {
    level.sendParticles(ParticleTypes.END_ROD, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, 60, 1.0, 1.0, 1.0, 0.15);
    level.sendParticles(ParticleTypes.ENCHANT, shieldPos.getX() + 0.5, shieldPos.getY() + 3.0, shieldPos.getZ() + 0.5, 80, 2.0, 1.5, 2.0, 0.1);
    level.playSound(null, shieldPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.4f);
    level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
}

private static void spawnActivationParticles(Level level, BlockPos pos) {
    if (level.isClientSide()) {
        for (int i = 0; i < 50; i++) {
            double dx = (level.random.nextDouble() - 0.5D) * 4.0D;
            double dy = level.random.nextDouble() * 2.0D;
            double dz = (level.random.nextDouble() - 0.5D) * 4.0D;
            level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D, dx * 0.04D, dy * 0.04D, dz * 0.04D);
        }
        return;
    }
    ServerLevel serverLevel = (ServerLevel) level;
    serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 3.0D, pos.getZ() + 0.5D, 120, 2.0D, 2.0D, 2.0D, 0.1D);
    serverLevel.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.2f);
}
}
