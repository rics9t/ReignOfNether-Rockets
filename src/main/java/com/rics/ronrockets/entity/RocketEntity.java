package com.rics.ronrockets.entity;

import com.rics.ronrockets.shield.ShieldEnergyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

public class RocketEntity extends Entity {

    // === Константы ===
    private static final float SPEED = 0.55F;
    private static final float MAX_ARC_HEIGHT = 55.0F;
    private static final float EXPLOSION_RADIUS = 6.0F;
    private static final float EXPLOSION_DAMAGE = 40.0F;
    private static final int MAX_LIFETIME = 600; // 30 секунд максимум жизни

    // === Синхронизированные данные (клиент ↔ сервер) ===
    private static final EntityDataAccessor<BlockPos> TARGET_POS =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<String> ATTACKER_NAME =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.STRING);

    // === Полёт по кривой Безье ===
    private Vec3 startPos;
    private Vec3 controlPoint;
    private Vec3 endPos;
    private float progress = 0.0F;
    private boolean trajectoryInitialized = false;
    private int lifetime = 0;

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // ракета игнорирует коллизии блоков при движении
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_POS, BlockPos.ZERO);
        this.entityData.define(ATTACKER_NAME, "");
    }

    // === Сеттеры ===
    public void setTarget(BlockPos target) {
        this.entityData.set(TARGET_POS, target);
    }

    public void setAttacker(String name) {
        this.entityData.set(ATTACKER_NAME, name != null ? name : "");
    }

    // === Геттеры ===
    public BlockPos getTarget() {
        return this.entityData.get(TARGET_POS);
    }

    public String getAttacker() {
        return this.entityData.get(ATTACKER_NAME);
    }

    // === Инициализация траектории Безье ===
    private void initTrajectory() {
        if (trajectoryInitialized) return;

        this.startPos = this.position();
        BlockPos target = getTarget();
        this.endPos = new Vec3(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5);

        // Контрольная точка — середина пути, поднятая вверх
        double midX = (startPos.x + endPos.x) / 2.0;
        double midZ = (startPos.z + endPos.z) / 2.0;
        double distance = startPos.distanceTo(endPos);
        double arcHeight = Math.min(MAX_ARC_HEIGHT, distance * 0.5);
        double midY = Math.max(startPos.y, endPos.y) + arcHeight;

        this.controlPoint = new Vec3(midX, midY, midZ);
        this.trajectoryInitialized = true;

        // Скорость полёта зависит от дистанции
        // progress += SPEED / distance каждый тик
    }

    // === Квадратичная кривая Безье ===
    private Vec3 bezier(float t) {
        float u = 1.0F - t;
        // B(t) = (1-t)²·P0 + 2(1-t)t·P1 + t²·P2
        return startPos.scale(u * u)
                .add(controlPoint.scale(2.0 * u * t))
                .add(endPos.scale(t * t));
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // Ждём пока целевая позиция синхронизируется
        if (getTarget().equals(BlockPos.ZERO)) return;

        initTrajectory();

        // Расчёт скорости прогресса
        double totalDistance = startPos.distanceTo(endPos);
        if (totalDistance < 1.0) totalDistance = 1.0;
        float progressStep = (float) (SPEED / totalDistance);

        progress += progressStep;

        if (progress >= 1.0F) {
            // === ПРИЗЕМЛЕНИЕ ===
            if (!level().isClientSide()) {
                onImpact();
            }
            this.discard();
            return;
        }

        // Двигаем ракету по кривой
        Vec3 newPos = bezier(progress);
        Vec3 oldPos = position();

        // Устанавливаем вектор движения (для рендерера — yaw/pitch)
        Vec3 motion = newPos.subtract(oldPos);
        this.setDeltaMovement(motion);
        this.setPos(newPos.x, newPos.y, newPos.z);

        // Проверка перехвата щитом (только сервер)
        if (!level().isClientSide()) {
            checkShieldIntercept();
        }

        // Частицы дыма на клиенте
        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    0, -0.2, 0);
            level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY() - 0.3, this.getZ(),
                    0, -0.15, 0);
        }
    }

    // === Удар при приземлении ===
    private void onImpact() {
        ServerLevel serverLevel = (ServerLevel) level();
        BlockPos target = getTarget();

        // Визуальный взрыв (частицы + звук, без разрушения блоков)
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5,
                5, 2.0, 2.0, 2.0, 0.1);

        serverLevel.playSound(null, target,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, 0.8F + random.nextFloat() * 0.4F);

        // Поджигаем центр
        if (serverLevel.getBlockState(target.above()).isAir()) {
            serverLevel.setBlock(target.above(), Blocks.FIRE.defaultBlockState(), 3);
        }

        // Урон всем сущностям в радиусе
        AABB damageArea = new AABB(target).inflate(EXPLOSION_RADIUS);
        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, damageArea);

        for (LivingEntity entity : entities) {
            double dist = entity.position().distanceTo(
                    new Vec3(target.getX() + 0.5, target.getY(), target.getZ() + 0.5));
            if (dist > EXPLOSION_RADIUS) continue;

            // Урон уменьшается с расстоянием
            float damageMult = 1.0F - (float) (dist / EXPLOSION_RADIUS);
            float damage = EXPLOSION_DAMAGE * damageMult;

            entity.hurt(damageSources().explosion(this, null), damage);

            // Отбрасывание
            Vec3 knockback = entity.position().subtract(
                    target.getX() + 0.5, target.getY(), target.getZ() + 0.5).normalize().scale(1.5 * damageMult);
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.x, 0.4 * damageMult, knockback.z));
        }
    }

    // === Проверка перехвата щитом ===
    private void checkShieldIntercept() {
        if (ShieldEnergyManager.isIntercepted(this)) {
            // Щит перехватил ракету
            ServerLevel serverLevel = (ServerLevel) level();
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    30, 1.0, 1.0, 1.0, 0.1);
            serverLevel.playSound(null, this.blockPosition(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS,
                    3.0F, 1.2F);
            this.discard();
        }
    }

    // === Сериализация NBT ===
    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        if (tag.contains("TargetX")) {
            setTarget(new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ")));
        }
        if (tag.contains("Attacker")) {
            setAttacker(tag.getString("Attacker"));
        }
        this.progress = tag.getFloat("Progress");
        this.lifetime = tag.getInt("Lifetime");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        BlockPos target = getTarget();
        tag.putInt("TargetX", target.getX());
        tag.putInt("TargetY", target.getY());
        tag.putInt("TargetZ", target.getZ());
        tag.putString("Attacker", getAttacker());
        tag.putFloat("Progress", this.progress);
        tag.putInt("Lifetime", this.lifetime);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
