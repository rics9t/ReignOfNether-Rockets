package com.rics.ronrockets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RocketClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final List<TrackedRocket> INCOMING_ROCKETS = new ArrayList<>();

    private static final int MAX_LIFETIME_TICKS = 600;

    public static void addIncomingRocket(BlockPos pos) {
        INCOMING_ROCKETS.add(new TrackedRocket(pos, 0));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (MC.level == null) {
            INCOMING_ROCKETS.clear();
            return;
        }

        Iterator<TrackedRocket> it = INCOMING_ROCKETS.iterator();
        while (it.hasNext()) {
            TrackedRocket tracked = it.next();
            tracked.age++;

            if (tracked.age > MAX_LIFETIME_TICKS) {
                it.remove();
                continue;
            }

            MC.level.addParticle(
                    ParticleTypes.SMOKE,
                    tracked.pos.getX() + 0.5,
                    tracked.pos.getY() + 5,
                    tracked.pos.getZ() + 0.5,
                    0, -0.2, 0
            );
        }
    }

    private static class TrackedRocket {
        final BlockPos pos;
        int age;

        TrackedRocket(BlockPos pos, int age) {
            this.pos = pos;
            this.age = age;
        }
    }
}
