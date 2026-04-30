package com.rics.ronrockets.entity;

import com.rics.ronrockets.RonRocketsConfig;
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

    /** Flight progress 0..1 */
    public float getFlightProgress() {
        if (maxFlightTicks == 0) return 0;
        return (float) flightTicks / maxFlightTicks;
    }

    public BlockPos getTarget() {
        return target;
    }

    public String getAttackerName() {
        return attacker == null ? "" : attacker;
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

            // Speed from config (default ~1.4, roughly 2.5x the original 0.55)
            double speed = RonRocketsConfig.getRocketSpeed();
            maxFlightTicks = Math.max(20, (int) Math.ceil(dist / speed));

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
                            getAttackerName(),
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
     * Simplified trail — smoke only, with random scatter and slight drift.
     * Keeps performance light: ~3 particles per tick max.
     */
    private void spawnTrailParticles() {
        Level lvl = level();
        double x = getX();
        double y = getY();
        double z = getZ();
        boolean descending = getDeltaMovement().y < 0;

        // Primary smoke — campfire smoke with random scatter and drift
        double scatter = 0.5;
        double driftY = descending ? 0.06 : -0.06;
        lvl.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x + (random.nextDouble() - 0.5) * scatter,
                y - 0.6,
                z + (random.nextDouble() - 0.5) * scatter,
                (random.nextDouble() - 0.5) * 0.08,
                driftY + (random.nextDouble() - 0.5) * 0.02,
                (random.nextDouble() - 0.5) * 0.08
        );

        // Larger lingering smoke every other tick for volume
        if (tickCount % 2 == 0) {
            lvl.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + (random.nextDouble() - 0.5) * scatter * 1.2,
                    y - 1.0,
                    z + (random.nextDouble() - 0.5) * scatter * 1.2,
                    (random.nextDouble() - 0.5) * 0.04,
                    driftY * 0.6,
                    (random.nextDouble() - 0.5) * 0.04
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
