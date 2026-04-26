package com.rics.ronrockets.building;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class RocketBuildings {

    public static VillagerRocketSilo VILLAGER_SILO;
    public static MonsterRocketSilo MONSTER_SILO;
    public static PiglinRocketSilo PIGLIN_SILO;
    public static ShieldArrayBuilding SHIELD_ARRAY;

    public static void register() {

        VILLAGER_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                new ResourceLocation(RonRocketsMod.MODID, "villager_rocket_silo"),
                new VillagerRocketSilo()
        );

        MONSTER_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                new ResourceLocation(RonRocketsMod.MODID, "monster_rocket_silo"),
                new MonsterRocketSilo()
        );

        PIGLIN_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                new ResourceLocation(RonRocketsMod.MODID, "piglin_rocket_silo"),
                new PiglinRocketSilo()
        );

        SHIELD_ARRAY = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                new ResourceLocation(RonRocketsMod.MODID, "shield_array"),
                new ShieldArrayBuilding("shield_array")
        );
    }
}
