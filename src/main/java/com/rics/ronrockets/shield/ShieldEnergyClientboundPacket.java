package com.rics.ronrockets.shield;

import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.registrars.PacketHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ShieldEnergyClientboundPacket {

    private final BlockPos originPos;
    private final int energy;

    public ShieldEnergyClientboundPacket(BlockPos originPos, int energy) {
        this.originPos = originPos;
        this.energy = energy;
    }

    public ShieldEnergyClientboundPacket(FriendlyByteBuf buffer) {
        this.originPos = buffer.readBlockPos();
        this.energy = buffer.readInt();
    }

    public static void syncEnergy(BlockPos originPos, int energy) {
        PacketHandler.INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new ShieldEnergyClientboundPacket(originPos, energy)
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.originPos);
        buffer.writeInt(this.energy);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        AtomicBoolean success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ShieldEnergyManager.setEnergy(this.originPos, this.energy);
            BuildingPlacement placement = BuildingUtils.findBuilding(true, this.originPos);
            if (placement != null) {
                placement.updateButtons();
            }
            success.set(true);
        }));
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
