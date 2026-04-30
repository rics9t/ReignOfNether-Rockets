package com.rics.ronrockets;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Simple server-side config for the RonRockets addon.
 * Values are synced automatically by Forge on world load.
 * Keep this minimal — only truly tunable parameters belong here.
 */
public class RonRocketsConfig {

    public static final ForgeConfigSpec SPEC;

    // ── Config values ────────────────────────────────────────────
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
    }

    // Convenience accessors (avoids .get() boilerplate at call sites)
    public static double getRocketSpeed()       { return ROCKET_SPEED.get(); }
    public static int    getSiloLimit()          { return SILO_LIMIT_PER_PLAYER.get(); }
    public static int    getRocketStorageLimit() { return ROCKET_STORAGE_LIMIT.get(); }
}
