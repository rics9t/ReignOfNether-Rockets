package com.rics.ronrockets;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common config for the RonRockets addon (available on both client and server).
 * Values are synced automatically by Forge on world load.
 */
public class RonRocketsConfig {
    private static final Logger LOG = LogManager.getLogger("RonRockets");

    public static final ForgeConfigSpec SPEC;

    // Shield settings
    public static final ForgeConfigSpec.DoubleValue SHIELD_DAMAGE_FRACTION;
    public static final ForgeConfigSpec.DoubleValue SHIELD_REPAIR_SPEED_MULT;
    public static final ForgeConfigSpec.DoubleValue SHIELD_ACTIVE_DURATION_SEC;
    public static final ForgeConfigSpec.DoubleValue SHIELD_IRON_COST;
    public static final ForgeConfigSpec.IntValue SHIELD_COOLDOWN_SEC;

    // Rocket settings
    public static final ForgeConfigSpec.DoubleValue ROCKET_SPEED;
    public static final ForgeConfigSpec.IntValue SILO_LIMIT_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue ROCKET_STORAGE_LIMIT;

    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.comment("RonRockets addon settings").push("ronrockets");

        builder.push("shield");
        SHIELD_DAMAGE_FRACTION = builder
            .comment("Fraction of blocks destroyed when shield intercepts (0.20 = 20%).")
            .defineInRange("shieldDamageFraction", 0.20, 0.1, 0.9);
        SHIELD_REPAIR_SPEED_MULT = builder
            .comment("Repair speed multiplier. 1.0 = normal RoN speed, 2.0 = 2x faster.")
            .defineInRange("shieldRepairSpeedMult", 0.5, 0.1, 5.0);
        SHIELD_ACTIVE_DURATION_SEC = builder
            .comment("Duration shield stays active after use (seconds). Default 5.")
            .defineInRange("shieldActiveDurationSec", 5.0, 1.0, 30.0);
        SHIELD_IRON_COST = builder
            .comment("Iron cost per shield use. Default 50.")
            .defineInRange("shieldIronCost", 50.0, 0.0, 500.0);
        SHIELD_COOLDOWN_SEC = builder
            .comment("Cooldown between shield uses (seconds). Default 60.")
            .defineInRange("shieldCooldownSec", 60, 0, 300);
        builder.pop();

        builder.push("rocket");
        ROCKET_SPEED = builder
            .comment("Rocket flight speed in blocks per tick. Default 1.1.")
            .defineInRange("rocketSpeed", 1.1, 0.2, 5.0);
        SILO_LIMIT_PER_PLAYER = builder
            .comment("Maximum Rocket Silos a single player can own. Default 1 (ignored in sandbox).")
            .defineInRange("siloLimitPerPlayer", 1, 0, 10);
        ROCKET_STORAGE_LIMIT = builder
            .comment("Maximum rockets a silo can hold (stored + in production). Default 2.")
            .defineInRange("rocketStorageLimit", 2, 1, 10);
        builder.pop();

        builder.pop();
        SPEC = builder.build();
        LOG.info("RonRockets config SPEC built successfully");
    }

    // Shield accessors
    public static float getShieldDamageFraction() {
        try { return SHIELD_DAMAGE_FRACTION.get().floatValue(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default shieldDamageFraction=0.20", e); return 0.20f; }
    }

    public static float getShieldRepairSpeedMult() {
        try { return SHIELD_REPAIR_SPEED_MULT.get().floatValue(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default shieldRepairSpeedMult=1.0", e); return 1.0f; }
    }

    public static int getShieldActiveDurationTicks() {
        try { return (int) (SHIELD_ACTIVE_DURATION_SEC.get().doubleValue() * 20); }
        catch (Exception e) { LOG.warn("Config not loaded, using default shieldActiveDurationSec=5", e); return 100; }
    }

    public static double getShieldIronCost() {
        try { return SHIELD_IRON_COST.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default shieldIronCost=50", e); return 50.0; }
    }

    public static int getShieldCooldownSec() {
        try { return SHIELD_COOLDOWN_SEC.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default shieldCooldownSec=60", e); return 60; }
    }

    // Rocket accessors
    public static double getRocketSpeed() {
        try { return ROCKET_SPEED.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default rocketSpeed=1.1", e); return 1.1; }
    }

    public static int getSiloLimit() {
        try { return SILO_LIMIT_PER_PLAYER.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default siloLimit=1", e); return 1; }
    }

    public static int getRocketStorageLimit() {
        try { return ROCKET_STORAGE_LIMIT.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default rocketStorageLimit=2", e); return 2; }
    }
}
