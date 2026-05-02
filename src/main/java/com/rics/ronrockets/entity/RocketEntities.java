package com.rics.ronrockets.entity;

import com.rics.ronrockets.RonRocketsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RocketEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RonRocketsMod.MODID);

    public static final RegistryObject<EntityType<RocketEntity>> ROCKET =
        ENTITIES.register("rocket",
            () -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                .sized(0.5f, 1.5f)
                .clientTrackingRange(128)
                .updateInterval(1)
                .fireImmune()
                .build("rocket"));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ENTITIES.register(bus);
    }
}
