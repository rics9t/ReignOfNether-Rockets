package com.rics.ronrockets.network;

import com.rics.ronrockets.RonRocketsMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RonRocketsNetwork {

    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static boolean initialised = false;

    private RonRocketsNetwork() {
    }

    public static void init() {
        if (initialised) {
            return;
        }

        int index = 0;
        CHANNEL.messageBuilder(ShieldActivateServerboundPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(ShieldActivateServerboundPacket::encode)
            .decoder(ShieldActivateServerboundPacket::new)
            .consumerMainThread(ShieldActivateServerboundPacket::handle)
            .add();

        CHANNEL.messageBuilder(RocketWarningClientboundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(RocketWarningClientboundPacket::encode)
            .decoder(RocketWarningClientboundPacket::new)
            .consumerMainThread(RocketWarningClientboundPacket::handle)
            .add();

        CHANNEL.messageBuilder(ScreenShakeClientboundPacket.class, index, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ScreenShakeClientboundPacket::encode)
            .decoder(ScreenShakeClientboundPacket::new)
            .consumerMainThread(ScreenShakeClientboundPacket::handle)
            .add();

        initialised = true;
    }
}
