package com.rics.ronrockets.entity;

import com.rics.ronrockets.rocket.RocketManager;
import com.rics.ronrockets.rocket.RocketStrike;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class RocketEntity extends Entity {

    private BlockPos target;
    private String attacker;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;
        if (target == null) return;

        double dx = target.getX() + 0.5 - getX();
        double dz = target.getZ() + 0.5 - getZ();
        double dy = target.getY() + 1 - getY();

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // ✅ Banana arc
        double arcHeight = 0.03 * horizontalDist;

        setDeltaMovement(
                dx * 0.02,
                dy * 0.02 + arcHeight,
                dz * 0.02
        );

        move(MoverType.SELF, getDeltaMovement());

        if (horizontalDist < 2.0) {

            RocketManager.resolveStrikeFromEntity(
                    new RocketStrike(attacker, blockPosition(), target, 0),
                    (net.minecraft.server.level.ServerLevel) level()
            );

            discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
