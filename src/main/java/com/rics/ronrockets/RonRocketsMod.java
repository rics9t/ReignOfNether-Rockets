package com.rics.ronrockets;

import com.rics.ronrockets.building.RocketBuildings;
import com.rics.ronrockets.building.RocketPlacementHandler;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.network.RonRocketsNetwork;
import com.rics.ronrockets.rocket.RocketProduction;
import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.rics.ronrockets.shield.ShieldVisualTickHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RonRocketsMod.MODID)
public class RonRocketsMod {

    public static final String MODID = "ronrockets";
    private static final Logger LOG = LogManager.getLogger("RonRockets");

    public RonRocketsMod() {
        LOG.info("RonRocketsMod constructor starting...");

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        try {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RonRocketsConfig.SPEC);
            LOG.info("Config registered OK");
        } catch (Exception e) {
            LOG.error("FAILED to register config!", e);
        }

        try {
            RonRocketsNetwork.init();
            LOG.info("Network init OK");
        } catch (Exception e) {
            LOG.error("FAILED to init network!", e);
        }

        try {
            RocketEntities.register(modBus);
            LOG.info("Entities registered OK");
        } catch (Exception e) {
            LOG.error("FAILED to register entities!", e);
        }

        try {
            RocketProduction.init();
            LOG.info("RocketProduction init OK");
        } catch (Exception e) {
            LOG.error("FAILED to init RocketProduction!", e);
        }

        try {
            RocketBuildings.register();
            LOG.info("Buildings registered OK");
        } catch (Exception e) {
            LOG.error("FAILED to register buildings!", e);
        }

        MinecraftForge.EVENT_BUS.register(ShieldEnergyManager.class);
        MinecraftForge.EVENT_BUS.register(ShieldVisualTickHandler.class);
        MinecraftForge.EVENT_BUS.register(RocketPlacementHandler.class);

        LOG.info("RonRocketsMod constructor finished successfully");
    }
}
