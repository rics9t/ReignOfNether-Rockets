package com.rics.ronrockets;

import com.rics.ronrockets.building.RocketBuildings;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.rocket.RocketManager;
import com.rics.ronrockets.shield.ShieldStateManager;
import com.rics.ronrockets.client.RocketClientEvents;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RonRocketsMod.MODID)
public class RonRocketsMod {

    public static final String MODID = "ronrockets";

    public RonRocketsMod() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // ✅ Register entity types
        RocketEntities.register(modBus);

        // ✅ Register buildings
        RocketBuildings.register();

        // ✅ Register server tick systems
        MinecraftForge.EVENT_BUS.register(RocketManager.class);
        MinecraftForge.EVENT_BUS.register(ShieldStateManager.class);

        // ✅ Register client tick system
        MinecraftForge.EVENT_BUS.register(RocketClientEvents.class);
    }
}
