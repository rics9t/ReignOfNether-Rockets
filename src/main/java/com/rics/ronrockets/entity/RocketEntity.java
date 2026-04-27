package com.rics.ronrockets.entity;

import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.rocket.RocketManager;
import com.rics.ronrockets.rocket.RocketStrike;
import com.rics.ronrockets.shield.ShieldStateManager;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class RocketEntity extends Entity {

    private BlockPos target;
    private String attacker;

    private double startX, startY, startZ;
    private int flightTicks = 0;
    private int maxFlightTicks = 0;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) { super(type, level); }

    public void setTarget(BlockPos target) { this.target = target; }
    public void setAttacker(String attacker) { this.attacker = attacker; }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            for(int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() - 0.5, getZ(), 0, -0.1, 0);
                level().addParticle(ParticleTypes.FLAME, getX(), getY() - 0.5, getZ(), 0, -0.1, 0);
            }
            return;
        }

        // ✅ Mid-Air Shield Interception Physics!
        ServerLevel serverLevel = (ServerLevel) level();
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            if (building.getBuilding() instanceof ShieldArrayBuilding && building.isBuilt && ShieldStateManager.isActive(building)) {
                if (building.centrePos.distToCenterSqr(getX(), getY(), getZ()) <= ShieldArrayBuilding.SHIELD_RADIUS * ShieldArrayBuilding.SHIELD_RADIUS) {
                    
                    // Massive Mid-Air Explosion
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 3, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 200, 3, 3, 3, 0.1);
                    serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 5.0f, 1.0f);
                    
                    this.discard();
                    return; // Destroyed
                }
            }
        }

        if (target != null && maxFlightTicks == 0) {
            startX = getX(); startY = getY(); startZ = getZ();
            double dist = Math.sqrt(target.distToCenterSqr(startX, startY, startZ));
            maxFlightTicks = Math.max(20, (int) (dist / 1.5));
        }

        if (target == null) return;

        flightTicks++;
        if (flightTicks >= maxFlightTicks) {
            RocketManager.resolveStrikeFromEntity(new RocketStrike(attacker, new BlockPos((int)startX, (int)startY, (int)startZ), target, 0), serverLevel);
            discard();
            return;
        }

        double p = (double) flightTicks / maxFlightTicks;
        double nextX = startX + (target.getX() + 0.5 - startX) * p;
        double nextZ = startZ + (target.getZ() + 0.5 - startZ) * p;

        double horizontalDist = Math.sqrt(target.distToCenterSqr(startX, startY, startZ));
        double arc = Math.min(horizontalDist * 0.4, 40.0);
        double nextY = startY + (target.getY() + 1 - startY) * p + arc * 4 * p * (1 - p);

        setDeltaMovement(nextX - getX(), nextY - getY(), nextZ - getZ());
        setPos(nextX, nextY, nextZ);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (tag.contains("TargetX")) target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        attacker = tag.getString("Attacker");
        startX = tag.getDouble("StartX"); startY = tag.getDouble("StartY"); startZ = tag.getDouble("StartZ");
        flightTicks = tag.getInt("FlightTicks"); maxFlightTicks = tag.getInt("MaxFlightTicks");
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (target != null) {
            tag.putInt("TargetX", target.getX()); tag.putInt("TargetY", target.getY()); tag.putInt("TargetZ", target.getZ());
        }
        if (attacker != null) tag.putString("Attacker", attacker);
        tag.putDouble("StartX", startX); tag.putDouble("StartY", startY); tag.putDouble("StartZ", startZ);
        tag.putInt("FlightTicks", flightTicks); tag.putInt("MaxFlightTicks", maxFlightTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
