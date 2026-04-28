package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.ShieldArrayBuilding;
import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class ShieldInterceptAbility extends Ability {

    private static final int COOLDOWN = 30 * 20;
    private static final int ACTIVE_DURATION = 10 * 20;
    private static final int ACTIVATION_COST = 250;

    public ShieldInterceptAbility() {
        super(UnitAction.ACTIVATE_SHIELD_ARRAY, COOLDOWN, ShieldArrayBuilding.SHIELD_RADIUS, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        int energy = ShieldEnergyManager.getEnergy(placement);
        AbilityButton button = new AbilityButton(
                "Shield Intercept",
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
                hotkey,
                () -> isShieldActive(placement),
                () -> false,
                () -> ShieldEnergyManager.getEnergy(placement) >= ACTIVATION_COST && isOffCooldown(placement),
                () -> UnitClientEvents.sendUnitCommand(this.action),
                null,
                List.of(
                        fcs("Activate Shield", true),
                        fcs("Energy: " + energy + "/" + ShieldEnergyManager.getMaxEnergy()),
                        fcs("Activation Cost: " + ACTIVATION_COST),
                        fcs("Intercept Radius: " + ShieldArrayBuilding.SHIELD_RADIUS),
                        fcs("Duration: 10s"),
                        fcs("Cooldown: 30s")
                ),
                this,
                placement
        );
        button.extraLabel = String.valueOf(energy);
        button.extraLabelColour = energy >= ACTIVATION_COST ? 0x55FF55 : 0xFF5555;
        return button;
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (ShieldEnergyManager.getEnergy(buildingUsing) < ACTIVATION_COST) {
            if (level.isClientSide()) {
                HudClientEvents.showTemporaryMessage("Not enough shield energy");
            }
            return;
        }

        if (level.isClientSide()) {
            ShieldEnergyManager.setEnergy(buildingUsing, ShieldEnergyManager.getEnergy(buildingUsing) - ACTIVATION_COST);
            this.setToMaxCooldown(buildingUsing);
            buildingUsing.updateButtons();
            spawnActivationParticles(level, buildingUsing.centrePos);
            return;
        }

        if (!ShieldEnergyManager.consumeEnergy(buildingUsing, ACTIVATION_COST)) {
            return;
        }

        this.setToMaxCooldown(buildingUsing);
        ShieldEnergyManager.syncEnergy(buildingUsing);
        buildingUsing.updateButtons();
        spawnActivationParticles(level, buildingUsing.centrePos);
    }

    public static ShieldInterceptAbility getFrom(BuildingPlacement placement) {
        for (var ability : placement.getAbilities()) {
            if (ability instanceof ShieldInterceptAbility shieldAbility) {
                return shieldAbility;
            }
        }
        return null;
    }

    public boolean isShieldActive(BuildingPlacement placement) {
        return getCooldown(placement) > (cooldownMax - ACTIVE_DURATION);
    }

    public static int getActivationCost() {
        return ACTIVATION_COST;
    }

    public static void spawnInterceptParticles(ServerLevel level, BlockPos shieldPos, BlockPos targetPos) {
        level.sendParticles(
                ParticleTypes.END_ROD,
                targetPos.getX() + 0.5,
                targetPos.getY() + 1.0,
                targetPos.getZ() + 0.5,
                60,
                1.0,
                1.0,
                1.0,
                0.15
        );
        level.sendParticles(
                ParticleTypes.ENCHANT,
                shieldPos.getX() + 0.5,
                shieldPos.getY() + 3.0,
                shieldPos.getZ() + 0.5,
                80,
                2.0,
                1.5,
                2.0,
                0.1
        );
        level.playSound(
                null,
                shieldPos,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS,
                1.2f,
                1.4f
        );
        level.playSound(
                null,
                targetPos,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                1.0f,
                0.8f
        );
    }

    private static void spawnActivationParticles(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            for (int i = 0; i < 50; i++) {
                double dx = (level.random.nextDouble() - 0.5D) * 4.0D;
                double dy = level.random.nextDouble() * 2.0D;
                double dz = (level.random.nextDouble() - 0.5D) * 4.0D;
                level.addParticle(
                        ParticleTypes.ENCHANT,
                        pos.getX() + 0.5D,
                        pos.getY() + 3.0D,
                        pos.getZ() + 0.5D,
                        dx * 0.04D,
                        dy * 0.04D,
                        dz * 0.04D
                );
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                pos.getX() + 0.5D,
                pos.getY() + 3.0D,
                pos.getZ() + 0.5D,
                120,
                2.0D,
                2.0D,
                2.0D,
                0.1D
        );
        serverLevel.playSound(
                null,
                pos,
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.BLOCKS,
                1.0f,
                1.2f
        );
    }
}
