package com.rics.ronrockets.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RocketLayers {

    public static final ModelLayerLocation ROCKET_LAYER =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath("ronrockets", "rocket"),
                    "main"
            );
}
