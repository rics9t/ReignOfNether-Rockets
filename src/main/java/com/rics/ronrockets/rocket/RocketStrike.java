package com.rics.ronrockets.rocket;

import net.minecraft.core.BlockPos;

public class RocketStrike {

    public final String attacker;
    public final BlockPos launchPos;
    public final BlockPos targetPos;
    public final long impactTick;

    public RocketStrike(String attacker,
                        BlockPos launchPos,
                        BlockPos targetPos,
                        long impactTick) {
        this.attacker = attacker;
        this.launchPos = launchPos;
        this.targetPos = targetPos;
        this.impactTick = impactTick;
    }
}