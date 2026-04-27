package com.rics.ronrockets.entity;

import com.rics.ronrockets.ability.ShieldInterceptAbility;
import com.rics.ronrockets.shield.ShieldEnergyManager;
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

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
    }

    public void setTarget(BlockPos target) { this.target = target; }
    public void setAttacker(String attacker) { this.attacker = attacker; }

    @Override protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, -0.1, 0);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();

        // Shield interception
        for (BuildingPlacement building : BuildingServerEvents.getBuildings()) {
            building.getAbilities().forEach(ability -> {
                if (ability instanceof ShieldInterceptAbility shield) {
                    if (shield.isShieldActive(building) &&
                        building.centrePos.distToCenterSqr(getX(), getY(), getZ()) <= 64 * 64) {

                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                                getX(), getY(), getZ(),
                                3, 0, 0, 0, 0);

                        this.discard();
                    }
                }
            });
        }

        if (target == null) return;

        if (maxFlightTicks == 0) {
            startX = getX();
            startY = getY();
            startZ = getZ();
            double dist = Math.sqrt(target.distToCenterSqr(startX, startY, startZ));
            maxFlightTicks = Math.max(20, (int)(dist / 1.5));
        }

        flightTicks++;

        if (flightTicks >= maxFlightTicks) {
            serverLevel.explode(null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    6f,
                    Level.ExplosionInteraction.TNT);

            discard();
            return;
        }

        double p = (double) flightTicks / maxFlightTicks;

        double nextX = startX + (target.getX() - startX) * p;
        double nextZ = startZ + (target.getZ() - startZ) * p;
        double arc = 30 * 4 * p * (1 - p);
        double nextY = startY + arc;

        setPos(nextX, nextY, nextZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
