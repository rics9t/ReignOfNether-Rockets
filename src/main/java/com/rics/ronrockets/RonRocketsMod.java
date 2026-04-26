package com.rics.ronrockets;

import com.rics.ronrockets.rocket.RocketManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(RonRocketsMod.MODID)
public class RonRocketsMod {

    public static final String MODID = "ronrockets";

    public RonRocketsMod() {

    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

    RocketBuildings.register(modBus);

    MinecraftForge.EVENT_BUS.register(RocketManager.class);
  }
}