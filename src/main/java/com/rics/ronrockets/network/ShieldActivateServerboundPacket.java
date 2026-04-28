package com.rics.ronrockets.network;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.shield.ShieldEnergyClientboundPacket;
import com.solegendary.reignofnether.alliance.AlliancesServerEvents;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.sandbox.SandboxServer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ShieldActivateServerboundPacket {

    private final BlockPos originPos;

    public ShieldActivateServerboundPacket(BlockPos originPos) {
        this.originPos = originPos;
    }

    public ShieldActivateServerboundPacket(FriendlyByteBuf buffer) {
        this.originPos = buffer.readBlockPos();
    }

    public static void send(BlockPos originPos) {
        RonRocketsNetwork.CHANNEL.sendToServer(new ShieldActivateServerboundPacket(originPos));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.originPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        AtomicBoolean success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            BuildingPlacement placement = BuildingUtils.findBuilding(false, this.originPos);
            if (placement == null) {
                return;
            }
            if (!(placement.getBuilding() instanceof ShieldArrayBuilding)) {
                return;
            }

            String playerName = player.getName().getString();
            boolean authorised = playerName.equals(placement.ownerName)
                    || SandboxServer.isAnyoneASandboxPlayer()
                    || AlliancesServerEvents.canControlAlly(playerName, placement.ownerName);

            if (!authorised) {
                return;
            }

            ShieldInterceptAbility ability = ShieldInterceptAbility.getFrom(placement);
            if (ability == null) {
                return;
            }

            ability.use(placement.getLevel(), placement, placement.centrePos);
            ShieldEnergyClientboundPacket.syncShieldState(placement);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
