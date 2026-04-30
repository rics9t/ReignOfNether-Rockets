package com.rics.ronrockets.network;

import com.rics.ronrockets.client.RocketClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Sent from server to all clients when a rocket impacts.
 * Triggers a camera shake effect with the given intensity and duration.
 */
public class ScreenShakeClientboundPacket {

    private final BlockPos impactPos;
    private final float intensity;
    private final int durationTicks;

    public ScreenShakeClientboundPacket(BlockPos impactPos, float intensity, int durationTicks) {
        this.impactPos = impactPos;
        this.intensity = intensity;
        this.durationTicks = durationTicks;
    }

    public ScreenShakeClientboundPacket(FriendlyByteBuf buf) {
        this.impactPos = buf.readBlockPos();
        this.intensity = buf.readFloat();
        this.durationTicks = buf.readInt();
    }

    public static void send(BlockPos impactPos, float intensity, int durationTicks) {
        RonRocketsNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new ScreenShakeClientboundPacket(impactPos, intensity, durationTicks)
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(impactPos);
        buf.writeFloat(intensity);
        buf.writeInt(durationTicks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        AtomicBoolean success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            RocketClientEvents.onScreenShake(impactPos, intensity, durationTicks);
            success.set(true);
        }));
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
