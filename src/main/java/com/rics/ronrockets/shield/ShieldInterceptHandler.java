package com.rics.ronrockets.shield;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.entity.RocketEntity;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ShieldInterceptHandler {

    private static final int INTERCEPT_ENERGY_COST = 50;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!placement.isBuilt) continue;
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!(placement.getLevel() instanceof ServerLevel serverLevel)) continue;

            ShieldInterceptAbility shield = null;
            for (Ability a : placement.getAbilities()) {
                if (a instanceof ShieldInterceptAbility s) {
                    shield = s;
                    break;
                }
            }
            if (shield == null || !shield.isShieldActive(placement)) continue;

            double radius = ShieldArrayBuilding.SHIELD_RADIUS;

            List<RocketEntity> rockets = new ArrayList<>(
                    serverLevel.getEntitiesOfClass(RocketEntity.class,
                            new net.minecraft.world.phys.AABB(
                                    placement.centrePos.getX() - radius,
                                    placement.centrePos.getY() - radius,
                                    placement.centrePos.getZ() - radius,
                                    placement.centrePos.getX() + radius,
                                    placement.centrePos.getY() + radius,
                                    placement.centrePos.getZ() + radius
                            )
                    )
            );

            for (RocketEntity rocket : rockets) {
                double dist = Math.sqrt(rocket.distanceToSqr(
                        placement.centrePos.getX() + 0.5,
                        placement.centrePos.getY() + 0.5,
                        placement.centrePos.getZ() + 0.5
                ));

                if (dist > radius) continue;

                // Check if the rocket belongs to the same owner - don't intercept own rockets
                if (rocket.getAttacker() != null
                        && rocket.getAttacker().equals(placement.ownerName)) continue;

                // Try to consume per-intercept energy
                if (!ShieldEnergyManager.consumeEnergy(placement, INTERCEPT_ENERGY_COST)) continue;

                // Intercept: destroy rocket mid-air with visuals
                serverLevel.sendParticles(
                        ParticleTypes.FLASH,
                        rocket.getX(), rocket.getY(), rocket.getZ(),
                        3, 0, 0, 0, 0
                );
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        rocket.getX(), rocket.getY(), rocket.getZ(),
                        80, 2, 2, 2, 0.2
                );
                serverLevel.sendParticles(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        rocket.getX(), rocket.getY(), rocket.getZ(),
                        40, 1, 1, 1, 0.05
                );
                serverLevel.playSound(
                        null,
                        rocket.blockPosition(),
                        SoundEvents.GENERIC_EXPLODE,
                        SoundSource.MASTER,
                        3.0f, 1.5f
                );

                rocket.discard();
            }
        }
    }
}
