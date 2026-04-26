package com.rics.ronrockets;

import com.rics.ronrockets.building.RocketBuildings;
import com.rics.ronrockets.rocket.RocketManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RonRocketsMod.MODID)
public class RonRocketsMod {

    public static final String MODID = "ronrockets";

    public RonRocketsMod() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        RocketBuildings.register();

        MinecraftForge.EVENT_BUS.register(RocketManager.class);
        MinecraftForge.EVENT_BUS.register(RocketPlacementHandler.class);
    }
}
