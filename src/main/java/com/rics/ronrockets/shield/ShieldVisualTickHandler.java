package com.rics.ronrockets.shield;

import com.rics.ronrockets.RonRocketsConfig;
import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ShieldVisualTickHandler {

    private static final Logger LOG = LogManager.getLogger("RonRockets/Shield");

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        List<BuildingPlacement> toCancel = new ArrayList<>();

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!placement.isBuilt) continue;
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!(placement.getLevel() instanceof ServerLevel serverLevel)) continue;

            ShieldInterceptAbility shield = null;
            for (var a : placement.getAbilities()) {
                if (a instanceof ShieldInterceptAbility s) {
                    shield = s;
                    break;
                }
            }
            if (shield == null) continue;

            float healthPct = ShieldInterceptAbility.getHealthPercent(placement);

            // Smoke particles when building is damaged (not at full health)
            if (healthPct < 1.0f && placement.tickAge % 8 == 0) {
                double cx = placement.centrePos.getX() + 0.5;
                double cy = placement.centrePos.getY() + 2.0;
                double cz = placement.centrePos.getZ() + 0.5;
                int count = (int) ((1.0f - healthPct) * 6) + 1;
                serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx, cy, cz,
                    count, 1.5, 0.5, 1.5, 0.02
                );
                if (healthPct < 0.4f && placement.tickAge % 16 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        cx, cy, cz,
                        3, 0.8, 0.3, 0.8, 0.04
                    );
                }
            }

            // Enchant particles when shield is currently active
            if (shield.isShieldActive(placement) && placement.tickAge % 5 == 0) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    placement.centrePos.getX() + 0.5,
                    placement.centrePos.getY() + 3.0,
                    placement.centrePos.getZ() + 0.5,
                    25, 1.5, 1.0, 1.5, 0.08
                );
            }

            // Deferred damage: apply building damage AFTER the active window ends
            // Active window: cooldown in (cooldownMax - activeDuration, cooldownMax]
            // The tick when cooldown transitions from just-inside to just-outside the
            // active window is when we apply the self-damage as the cost of activation.
            shield.cooldownMax = RonRocketsConfig.getShieldCooldownSec() * 20;
            int cooldown = (int) shield.getCooldown(placement);
            int cooldownMax = (int) shield.cooldownMax;
            int activeDuration = shield.getActiveDurationTicks();
            int threshold = cooldownMax - activeDuration;

            // Was active last tick (cooldown was threshold+1), now just ended (cooldown == threshold)
            // We detect this as: cooldown == threshold (the first tick outside the active window)
            if (cooldownMax > 0 && activeDuration > 0 && cooldown == threshold) {
                float damageFraction = RonRocketsConfig.getShieldDamageFraction();
                int blocksToDestroy = (int) (placement.getBlocksTotal() * damageFraction);
                LOG.info("Shield active window ended — destroying {} blocks ({}%)",
                    blocksToDestroy, (int)(damageFraction * 100));
                if (blocksToDestroy > 0) {
                    placement.destroyRandomBlocks(blocksToDestroy);
                    if (placement.shouldBeDestroyed()) {
                        LOG.warn("Shield destroyed by self-damage after activation");
                        toCancel.add(placement);
                    }
                    placement.updateButtons();
                }
            }
        }

        // Remove destroyed buildings after iteration to avoid ConcurrentModificationException
        for (BuildingPlacement placement : toCancel) {
            BuildingServerEvents.cancelBuilding(placement, placement.ownerName);
        }
    }
}
