package com.rics.ronrockets.rocket;

import com.rics.ronrockets.building.AbstractRocketSilo;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocketManager {

    public static Map<BlockPos, Integer> storedRockets = new HashMap<>();
    public static Map<BlockPos, Integer> cooldownTicks = new HashMap<>();

    public static void finishRocketProduction(BlockPos pos) {
        int stored = storedRockets.getOrDefault(pos, 0);
        storedRockets.put(pos, Math.min(2, stored + 1));
        cooldownTicks.put(pos, 3600); 
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement placement : BuildingServerEvents.getBuildings()) {
            if (!(placement.getBuilding() instanceof AbstractRocketSilo)) continue;
            if (!placement.isBuilt) continue;

            BlockPos pos = placement.centrePos;
            int cool = cooldownTicks.getOrDefault(pos, 0);
            if (cool > 0) cooldownTicks.put(pos, cool - 1);
        }
    }

    public static void resolveStrikeFromEntity(RocketStrike strike, ServerLevel level) {
        int radius = 12;

        // ✅ Ground Impact Visuals
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, strike.targetPos.getX(), strike.targetPos.getY(), strike.targetPos.getZ(), 3, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, strike.targetPos.getX(), strike.targetPos.getY(), strike.targetPos.getZ(), 250, 5, 5, 5, 0.15);
        level.playSound(null, strike.targetPos.getX(), strike.targetPos.getY(), strike.targetPos.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 5.0f, 1.0f);

        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;
            if (building.centrePos.distSqr(strike.targetPos) <= radius * radius) {
                AttackWarningClientboundPacket.sendWarning(building.ownerName, strike.targetPos);
            }
        }

        // Damage buildings
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (!building.isBuilt) continue;

            double distSqr = building.centrePos.distSqr(strike.targetPos);
            if (distSqr > radius * radius) continue;

            double falloff = Math.max(0, 1.0 - (Math.sqrt(distSqr) / radius));
            float maxPercent = building.isCapitol ? 0.30f : 0.60f;

            int blocksToDestroy = (int) (building.getBlocksTotal() * maxPercent * falloff);

            if (blocksToDestroy > 0) {
                building.destroyRandomBlocks(blocksToDestroy);
                if (building.shouldBeDestroyed()) BuildingServerEvents.cancelBuilding(building, building.ownerName);
            }
        }

        // Damage units
        AABB area = new AABB(strike.targetPos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity instanceof Unit) {
                double dist = Math.sqrt(entity.distanceToSqr(strike.targetPos.getX(), strike.targetPos.getY(), strike.targetPos.getZ()));
                if (dist <= radius) {
                    double falloff = 1.0 - (dist / radius);
                    float damage = (float) (200 * falloff);
                    if (damage > 0) entity.hurt(level.damageSources().generic(), damage);
                }
            }
        }
    }
}
