package com.solegendary.reignofnether.entities;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class NecromancerProjectile extends AbstractHurtingProjectile {

    private static final int MAX_TICKS = 60;

    public NecromancerProjectile(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public NecromancerProjectile(Level pLevel, LivingEntity pShooter, double offsetX, double offsetY, double offsetZ) {
        super(EntityRegistrar.NECROMANCER_PROJECTILE.get(), pShooter, offsetX, offsetY, offsetZ, pLevel);
    }

    @Override
    public void tick() {
        setDeltaMovement(getDeltaMovement().normalize());
        super.tick();
        if (tickCount > MAX_TICKS)
            this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (!this.level().isClientSide && this.getOwner() instanceof AttackerUnit aUnit) {
            pResult.getEntity().hurt(damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), aUnit.getUnitAttackDamage());
            MiscUtil.addParticleExplosion(ParticleTypes.WITCH, 10, level(), position());
            discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.WITCH;
    }
}
