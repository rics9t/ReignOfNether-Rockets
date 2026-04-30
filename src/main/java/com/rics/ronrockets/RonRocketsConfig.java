package com.rics.ronrockets;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common config for the RonRockets addon (available on both client and server).
 * Values are synced automatically by Forge on world load.
 * Keep this minimal — only truly tunable parameters belong here.
 */
public class RonRocketsConfig {

    private static final Logger LOG = LogManager.getLogger("RonRockets");

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue ROCKET_SPEED;
    public static final ForgeConfigSpec.IntValue SILO_LIMIT_PER_PLAYER;
    public static final ForgeConfigSpec.IntValue ROCKET_STORAGE_LIMIT;

    static {
        var builder = new ForgeConfigSpec.Builder();

        builder.comment("RonRockets addon settings").push("ronrockets");

        ROCKET_SPEED = builder
            .comment("Rocket flight speed in blocks per tick. Default 1.4 (~2.5x original).")
            .defineInRange("rocketSpeed", 1.4, 0.2, 5.0);

        SILO_LIMIT_PER_PLAYER = builder
            .comment("Maximum Rocket Silos a single player can own. Default 1.")
            .defineInRange("siloLimitPerPlayer", 1, 1, 10);

        ROCKET_STORAGE_LIMIT = builder
            .comment("Maximum rockets a silo can hold (stored + in production). Default 2.")
            .defineInRange("rocketStorageLimit", 2, 1, 10);

        builder.pop();

        SPEC = builder.build();
        LOG.info("RonRockets config SPEC built successfully");
    }

    // Safe accessors — fall back to defaults if config isn't loaded yet
    public static double getRocketSpeed() {
        try { return ROCKET_SPEED.get(); }
        catch (Exception e) { LOG.warn("Config not loaded, using default rocketSpeed=1.4", e); return 1.4; }
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
