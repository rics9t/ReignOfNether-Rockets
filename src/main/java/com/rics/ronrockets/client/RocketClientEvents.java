package com.rics.ronrockets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import com.rics.ronrockets.RonRocketsMod;

@Mod.EventBusSubscriber(modid = RonRocketsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final List<BlockPos> INCOMING_ROCKETS = new ArrayList<>();

    public static void addIncomingRocket(BlockPos pos) {
        INCOMING_ROCKETS.add(pos);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (MC.level == null) return;

        for (BlockPos pos : INCOMING_ROCKETS) {

            MC.level.addParticle(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5,
                    pos.getY() + 5,
                    pos.getZ() + 0.5,
                    0,
                    -0.2,
                    0
            );
        }
    }
}
