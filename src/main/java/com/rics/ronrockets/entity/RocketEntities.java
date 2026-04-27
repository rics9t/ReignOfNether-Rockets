package com.rics.ronrockets.entity;

import com.rics.ronrockets.RonRocketsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RocketEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RonRocketsMod.MODID);

    public static final RegistryObject<EntityType<RocketEntity>> ROCKET =
            ENTITIES.register("rocket", () ->
                    EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                            .sized(0.5F, 1.5F)           
                            .clientTrackingRange(128)    
                            .updateInterval(1)           
                            .build(new ResourceLocation(RonRocketsMod.MODID, "rocket").toString())
            );

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
