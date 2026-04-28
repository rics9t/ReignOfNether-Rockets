package com.rics.ronrockets.shield;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.network.RonRocketsNetwork;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingUtils;

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
    private final float cooldown;

    public ShieldEnergyClientboundPacket(BlockPos originPos, int energy, float cooldown) {
        this.originPos = originPos;
        this.energy = energy;
        this.cooldown = cooldown;
    }

    public ShieldEnergyClientboundPacket(FriendlyByteBuf buffer) {
        this.originPos = buffer.readBlockPos();
        this.energy = buffer.readInt();
        this.cooldown = buffer.readFloat();
    }

    public static void syncShieldState(BuildingPlacement placement) {
        ShieldInterceptAbility ability = ShieldInterceptAbility.getFrom(placement);
        RonRocketsNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new ShieldEnergyClientboundPacket(
                        placement.originPos,
                        ShieldEnergyManager.getEnergy(placement),
                        ability != null ? placement.getCooldown(ability) : 0
                )
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.originPos);
        buffer.writeInt(this.energy);
        buffer.writeFloat(this.cooldown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        AtomicBoolean success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ShieldEnergyManager.setEnergy(this.originPos, this.energy);
            BuildingPlacement placement = BuildingUtils.findBuilding(true, this.originPos);
            if (placement != null) {
                ShieldInterceptAbility ability = ShieldInterceptAbility.getFrom(placement);
                if (ability != null) {
                    placement.setCooldown(ability, this.cooldown);
                }
                placement.updateButtons();
            }
            success.set(true);
        }));
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
