package com.rics.ronrockets.client;

import com.rics.ronrockets.entity.RocketEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RocketEntities.ROCKET.get(), RocketRenderer::new);
    }

    @SubscribeEvent
public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(
            RocketLayers.ROCKET_LAYER,
            RocketModel::createBodyLayer
    );
}
}
