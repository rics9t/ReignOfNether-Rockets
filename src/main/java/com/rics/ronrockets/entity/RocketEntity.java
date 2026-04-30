package com.rics.ronrockets.entity;

import com.rics.ronrockets.rocket.RocketManager;
import com.rics.ronrockets.rocket.RocketStrike;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class RocketEntity extends Entity {

    private static final double BLOCKS_PER_TICK = 0.55;
    private static final double MAX_ARC_HEIGHT = 55.0;

    private BlockPos target;
    private String attacker;

    private double startX, startY, startZ;
    private int flightTicks = 0;
    private int maxFlightTicks = 0;

    // Quadratic Bezier control point
    private double ctrlX, ctrlY, ctrlZ;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    /**
     * Returns the flight progress 0..1
     */
    public float getFlightProgress() {
        if (maxFlightTicks == 0) return 0;
        return (float) flightTicks / maxFlightTicks;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnTrailParticles();
            return;
        }

        if (target == null) return;

        ServerLevel serverLevel = (ServerLevel) level();

        if (maxFlightTicks == 0) {
            startX = getX();
            startY = getY();
            startZ = getZ();

            double endX = target.getX() + 0.5;
            double endY = target.getY() + 1.0;
            double endZ = target.getZ() + 0.5;

            double dx = endX - startX;
            double dz = endZ - startZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            maxFlightTicks = Math.max(40, (int) Math.ceil(dist / BLOCKS_PER_TICK));

            double tCtrl = 0.30;
            ctrlX = startX + dx * tCtrl;
            ctrlZ = startZ + dz * tCtrl;

            double arcHeight = Math.min(MAX_ARC_HEIGHT, Math.max(18.0, dist * 0.28));
            ctrlY = Math.max(startY, endY) + arcHeight;
        }

        flightTicks++;
        if (flightTicks >= maxFlightTicks) {
            RocketManager.resolveStrikeFromEntity(
                    new RocketStrike(
                            attacker == null ? "" : attacker,
                            BlockPos.containing(startX, startY, startZ),
                            target,
                            serverLevel.getGameTime()
                    ),
                    serverLevel
            );
            discard();
            return;
        }

        double t = (double) flightTicks / (double) maxFlightTicks;

        double endX = target.getX() + 0.5;
        double endY = target.getY() + 1.0;
        double endZ = target.getZ() + 0.5;

        double oneMinusT = 1.0 - t;

        double nextX = oneMinusT * oneMinusT * startX + 2 * oneMinusT * t * ctrlX + t * t * endX;
        double nextY = oneMinusT * oneMinusT * startY + 2 * oneMinusT * t * ctrlY + t * t * endY;
        double nextZ = oneMinusT * oneMinusT * startZ + 2 * oneMinusT * t * ctrlZ + t * t * endZ;

        setDeltaMovement(nextX - getX(), nextY - getY(), nextZ - getZ());
        setPos(nextX, nextY, nextZ);
    }

    /**
     * Multi-layered client-side trail:
     *   1. Hot flame core right at the nozzle
     *   2. Dense smoke plume behind the rocket
     *   3. Spark shower for ignition feel
     *   4. Fading contrail of light smoke
     * Particle counts are kept low for performance; distance culling is implicit
     * since Minecraft won't render particles beyond 256 blocks.
     */
    private void spawnTrailParticles() {
        Level lvl = level();
        double x = getX();
        double y = getY();
        double z = getZ();
        boolean descending = getDeltaMovement().y < 0;

        // 1. Flame core — bright fire right at the engine
        for (int i = 0; i < 3; i++) {
            lvl.addParticle(ParticleTypes.FLAME,
                    x + (random.nextDouble() - 0.5) * 0.3,
                    y - 0.5,
                    z + (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.05,
                    descending ? 0.15 : -0.1,
                    (random.nextDouble() - 0.5) * 0.05
            );
        }

        // 2. Dense smoke plume — campfire smoke drifting behind
        lvl.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x + (random.nextDouble() - 0.5) * 0.4,
                y - 0.8,
                z + (random.nextDouble() - 0.5) * 0.4,
                (random.nextDouble() - 0.5) * 0.06,
                descending ? 0.08 : -0.06,
                (random.nextDouble() - 0.5) * 0.06
        );

        // Large smoke for volume (every other tick)
        if (tickCount % 2 == 0) {
            lvl.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + (random.nextDouble() - 0.5) * 0.5,
                    y - 1.0,
                    z + (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.04,
                    descending ? 0.06 : -0.04,
                    (random.nextDouble() - 0.5) * 0.04
            );
        }

        // 3. Sparks — small embers shooting out
        if (random.nextInt(3) == 0) {
            for (int i = 0; i < 2; i++) {
                lvl.addParticle(ParticleTypes.LAVA,
                        x + (random.nextDouble() - 0.5) * 0.4,
                        y - 0.3,
                        z + (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.15,
                        descending ? 0.2 + random.nextDouble() * 0.1 : -0.15 - random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.15
                );
            }
        }

        // 4. Contrail — faint lingering smoke trail (every 4 ticks to save particles)
        if (tickCount % 4 == 0) {
            lvl.addParticle(ParticleTypes.CLOUD,
                    x,
                    y - 1.5,
                    z,
                    0, 0.01, 0
            );
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("TargetX")) {
            target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        }
        if (tag.contains("Attacker")) {
            attacker = tag.getString("Attacker");
        }

        startX = tag.getDouble("StartX");
        startY = tag.getDouble("StartY");
        startZ = tag.getDouble("StartZ");

        ctrlX = tag.getDouble("CtrlX");
        ctrlY = tag.getDouble("CtrlY");
        ctrlZ = tag.getDouble("CtrlZ");

        flightTicks = tag.getInt("FlightTicks");
        maxFlightTicks = tag.getInt("MaxFlightTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (target != null) {
            tag.putInt("TargetX", target.getX());
            tag.putInt("TargetY", target.getY());
            tag.putInt("TargetZ", target.getZ());
        }
        if (attacker != null) {
            tag.putString("Attacker", attacker);
        }

        tag.putDouble("StartX", startX);
        tag.putDouble("StartY", startY);
        tag.putDouble("StartZ", startZ);

        tag.putDouble("CtrlX", ctrlX);
        tag.putDouble("CtrlY", ctrlY);
        tag.putDouble("CtrlZ", ctrlZ);

        tag.putInt("FlightTicks", flightTicks);
        tag.putInt("MaxFlightTicks", maxFlightTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
