package com.rics.ronrockets;

import com.rics.ronrockets.building.RocketBuildings;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.network.RonRocketsNetwork;
import com.rics.ronrockets.rocket.RocketProduction;
import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.rics.ronrockets.shield.ShieldVisualTickHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RonRocketsMod.MODID)
public class RonRocketsMod {

    public static final String MODID = "ronrockets";

    public RonRocketsMod() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        RonRocketsNetwork.init();
        RocketEntities.register(modBus);
        RocketProduction.init();
        RocketBuildings.register();

        MinecraftForge.EVENT_BUS.register(ShieldEnergyManager.class);
        MinecraftForge.EVENT_BUS.register(ShieldVisualTickHandler.class);
    }
}
