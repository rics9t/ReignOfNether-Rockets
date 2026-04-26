package com.rics.ronrockets.building;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RocketBuildings {

    public static final DeferredRegister<com.solegendary.reignofnether.building.Building> BUILDINGS =
            DeferredRegister.create(
                    ReignOfNetherRegistries.BUILDING,
                    RonRocketsMod.MODID
            );

    public static final RegistryObject<VillagerRocketSilo> VILLAGER_SILO =
            BUILDINGS.register("villager_rocket_silo", VillagerRocketSilo::new);

    public static final RegistryObject<MonsterRocketSilo> MONSTER_SILO =
            BUILDINGS.register("monster_rocket_silo", MonsterRocketSilo::new);

    public static final RegistryObject<PiglinRocketSilo> PIGLIN_SILO =
            BUILDINGS.register("piglin_rocket_silo", PiglinRocketSilo::new);

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BUILDINGS.register(bus);
    }
}