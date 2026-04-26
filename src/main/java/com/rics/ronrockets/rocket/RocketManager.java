package com.rics.ronrockets.rocket;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RocketManager {

    private static final List<RocketStrike> ACTIVE_STRIKES = new ArrayList<>();

    public static void registerStrike(RocketStrike strike) {
        ACTIVE_STRIKES.add(strike);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = BuildingServerEvents.getServerLevel();
        if (level == null) return;

        long currentTick = level.getGameTime();

        Iterator<RocketStrike> iterator = ACTIVE_STRIKES.iterator();

        while (iterator.hasNext()) {
            RocketStrike strike = iterator.next();

            if (currentTick >= strike.impactTick) {
                resolveStrike(strike, level);
                iterator.remove();
            }
        }
    }

    public static void resolveStrike(RocketStrike strike, ServerLevel level) {

        int radius = 8;

        // ✅ Shield interception
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

            if (!(building.getBuilding() instanceof ShieldArrayBuilding)) continue;
            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);

            if (dist <= ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) {

                for (var ability : building.getAbilities()) {
                    if (ability instanceof ShieldInterceptAbility) {
                        if (ability.isOffCooldown(building)) {
                            ability.setToMaxCooldown(building);
                            return; // intercepted
                        }
                    }
                }
            }
        }

        // ✅ Trigger native attack warning
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);
            if (dist <= radius * radius) {
                AttackWarningClientboundPacket.sendWarning(
                        building.ownerName,
                        strike.targetPos
                );
            }
        }

        // ✅ Damage buildings
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {

            if (!building.isBuilt) continue;

            double dist = building.centrePos.distSqr(strike.targetPos);
            if (dist > radius * radius) continue;

            float percent = building.isCapitol ? 0.40f : 0.80f;

            int blocksToDestroy = (int)(building.getBlocksTotal() * percent);

            building.destroyRandomBlocks(blocksToDestroy);

            if (building.shouldBeDestroyed()) {
                BuildingServerEvents.cancelBuilding(building, building.ownerName);
            }
        }

        // ✅ Damage units
        AABB area = new AABB(strike.targetPos).inflate(radius);

        List<LivingEntity> entities =
                level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity instanceof Unit) {
                entity.hurt(level.damageSources().generic(), 50f);
            }
        }

        // ✅ Spawn cinematic particles
        spawnImpactEffects(level, strike.targetPos);
    }

    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
    resolveStrike(strike, level);
    }

    private static void spawnImpactEffects(ServerLevel level, net.minecraft.core.BlockPos pos) {

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                1,
                0,
                0,
                0,
                0
        );

        level.playSound(
                null,
                pos,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.BLOCKS,
                2.0f,
                1.0f
        );
    }
}
