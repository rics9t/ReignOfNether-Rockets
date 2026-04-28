package com.rics.ronrockets.rocket;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.api.ReignOfNetherRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class RocketProduction {
    public static final RocketProd ROCKET_PROD = Registry.register(
            ReignOfNetherRegistries.PRODUCTION_ITEM,
            ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "rocket"),
            new RocketProd()
    );

    public static void init() {
    }
}
