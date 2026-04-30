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
 * Sent from server to all clients when a rocket is launched.
 * Carries the target position and attacker name so the client
 * can show a pre-impact warning indicator.
 */
public class RocketWarningClientboundPacket {

    private final BlockPos targetPos;
    private final String attackerName;

    public RocketWarningClientboundPacket(BlockPos targetPos, String attackerName) {
        this.targetPos = targetPos;
        this.attackerName = attackerName;
    }

    public RocketWarningClientboundPacket(FriendlyByteBuf buf) {
        this.targetPos = buf.readBlockPos();
        this.attackerName = buf.readUtf();
    }

    public static void send(BlockPos targetPos, String attackerName) {
        RonRocketsNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new RocketWarningClientboundPacket(targetPos, attackerName)
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(targetPos);
        buf.writeUtf(attackerName);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        AtomicBoolean success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            RocketClientEvents.onRocketWarningReceived(targetPos, attackerName);
            success.set(true);
        }));
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
