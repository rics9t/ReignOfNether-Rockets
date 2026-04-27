package com.rics.ronrockets.shield;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArray;
import com.rics.ronrockets.entity.RocketEntity;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShieldEnergyManager {

    private static final int MAX_ENERGY = 1000;
    private static final int REGEN_PER_TICK = 1; // ~50 энергии/сек
    private static final double SHIELD_RADIUS = 64.0;

    // Энергия для каждого здания (по ID размещения)
    private static final Map<Integer, Integer> energyMap = new ConcurrentHashMap<>();

    // === Публичный API ===

    public static int getEnergy(BuildingPlacement placement) {
        if (placement == null) return 0;
        return energyMap.getOrDefault(placement.hashCode(), MAX_ENERGY);
    }

    public static boolean consumeEnergy(BuildingPlacement placement, int amount) {
        if (placement == null) return false;
        int key = placement.hashCode();
        int current = energyMap.getOrDefault(key, MAX_ENERGY);
        if (current < amount) return false;
        energyMap.put(key, current - amount);
        return true;
    }

    /**
     * Проверяет, перехватывает ли какой-либо активный щит данную ракету.
     * Ракета перехватывается, если она:
     * 1) Находится в радиусе щита
     * 2) Щит активен (в первые 10 сек кулдауна)
     * 3) Ракета не принадлежит владельцу щита
     */
    public static boolean isIntercepted(RocketEntity rocket) {
        if (rocket == null || rocket.level().isClientSide()) return false;

        Vec3 rocketPos = rocket.position();
        String attackerName = rocket.getAttacker();

        for (BuildingPlacement bp : BuildingServerEvents.getBuildings()) {
            if (!(bp.getBuilding() instanceof ShieldArray)) continue;

            // Не перехватываем свои ракеты
            if (bp.ownerName != null && bp.ownerName.equals(attackerName)) continue;

            // Проверка радиуса
            Vec3 shieldCenter = new Vec3(
                    bp.centrePos.getX() + 0.5,
                    bp.centrePos.getY(),
                    bp.centrePos.getZ() + 0.5);
            double distance = rocketPos.distanceTo(shieldCenter);
            if (distance > SHIELD_RADIUS) continue;

            // Проверка активности щита
            if (ShieldInterceptAbility.isShieldActive(bp)) {
                return true;
            }
        }
        return false;
    }

    // === Серверный тик — регенерация энергии ===

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (BuildingPlacement bp : BuildingServerEvents.getBuildings()) {
            if (!(bp.getBuilding() instanceof ShieldArray)) continue;

            int key = bp.hashCode();
            int current = energyMap.getOrDefault(key, MAX_ENERGY);
            if (current < MAX_ENERGY) {
                energyMap.put(key, Math.min(MAX_ENERGY, current + REGEN_PER_TICK));
            }
        }
    }

    /**
     * Очистка при удалении здания (вызывать из обработчика удаления).
     */
    public static void removeBuilding(BuildingPlacement placement) {
        if (placement != null) {
            energyMap.remove(placement.hashCode());
        }
    }
}
