package com.rics.ronrockets.client;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.entity.RocketEntity;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = RonRocketsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketClientEvents {

    private static final Minecraft MC = Minecraft.getInstance();

    // Incoming warnings (enemy rockets targeting this player)
    private static final List<RocketMarker> INCOMING = new ArrayList<>();
    private static final int INCOMING_DURATION = 20 * 8;

    // Outgoing markers (this player's launched rockets)
    private static final List<RocketMarker> OUTGOING = new ArrayList<>();
    private static final int OUTGOING_DURATION = 20 * 10;

    // HUD warning state
    private static int hudWarningTicks = 0;

    /**
     * Called when the server broadcasts that a rocket has been launched.
     * Adds to INCOMING for non-attackers, and to OUTGOING for the attacker.
     * Also adds a pulsing marker on the minimap at the target position.
     */
    public static void onRocketWarningReceived(BlockPos targetPos, String attackerName) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        boolean isAttacker = player.getName().getString().equals(attackerName);

        if (isAttacker) {
            OUTGOING.add(new RocketMarker(targetPos, OUTGOING_DURATION));
        } else {
            // Play loud warning sound for incoming rockets
            MC.level.playSound(player, player.blockPosition(),
                SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 2.0f, 0.5f);
            // Secondary alert sound
            MC.level.playSound(player, player.blockPosition(),
                SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.HOSTILE, 1.5f, 0.3f);

            INCOMING.add(new RocketMarker(targetPos, INCOMING_DURATION));

            // Show HUD warning message
            hudWarningTicks = 20 * 5; // 5 seconds
            if (MC.gui != null) {
                MC.gui.setOverlayMessage(
                    Component.translatable("hud.ronrockets.rocket_incoming")
                        .withStyle(s -> s.withBold(true).withColor(net.minecraft.ChatFormatting.RED)),
                    false
                );
            }
        }

        // Pulsing target marker on the minimap for all players
        MinimapClientEvents.addMapMarker(targetPos.getX(), targetPos.getZ(), attackerName);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (MC.level == null) return;

        // Tick incoming warnings — smoke signal particles above target
        Iterator<RocketMarker> it = INCOMING.iterator();
        while (it.hasNext()) {
            RocketMarker w = it.next();
            w.ticksRemaining--;
            // Pulsing smoke signal above target
            if (w.ticksRemaining % 4 == 0) {
                MC.level.addParticle(
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    w.target.getX() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 2,
                    w.target.getY() + 6,
                    w.target.getZ() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 2,
                    0, -0.1, 0
                );
            }
            // Red flame particles — urgent visual
            if (w.ticksRemaining % 6 == 0) {
                MC.level.addParticle(
                    ParticleTypes.FLAME,
                    w.target.getX() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 1.5,
                    w.target.getY() + 5,
                    w.target.getZ() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 1.5,
                    0, 0.08, 0
                );
            }
            if (w.ticksRemaining <= 0) it.remove();
        }

        // Tick outgoing markers
        Iterator<RocketMarker> it2 = OUTGOING.iterator();
        while (it2.hasNext()) {
            RocketMarker m = it2.next();
            m.ticksRemaining--;
            if (m.ticksRemaining <= 0) it2.remove();
        }

        // Refresh HUD warning while active
        if (hudWarningTicks > 0) {
            hudWarningTicks--;
        }
    }

    // Expose for rendering
    public static List<RocketMarker> getIncomingWarnings() { return INCOMING; }
    public static List<RocketMarker> getOutgoingMarkers() { return OUTGOING; }

    // Screen shake state
    private static int shakeTicksRemaining = 0;
    private static float shakeIntensity = 0;

    public static void onScreenShake(BlockPos impactPos, float intensity, int durationTicks) {
        LocalPlayer player = MC.player;
        if (player != null) {
            double dist = player.distanceToSqr(
                impactPos.getX() + 0.5, impactPos.getY() + 0.5, impactPos.getZ() + 0.5);
            double falloff = Math.max(0, 1.0 - Math.sqrt(dist) / 200.0);
            shakeIntensity = intensity * (float) falloff;
        } else {
            shakeIntensity = intensity;
        }
        shakeTicksRemaining = durationTicks;
    }

    public static float getShakeOffsetX() {
        if (shakeTicksRemaining <= 0) return 0;
        return (MC.level.random.nextFloat() - 0.5f) * shakeIntensity * 2;
    }

    public static float getShakeOffsetY() {
        if (shakeTicksRemaining <= 0) return 0;
        return (MC.level.random.nextFloat() - 0.5f) * shakeIntensity * 2;
    }

    public static boolean isScreenShaking() { return shakeTicksRemaining > 0; }

    @SubscribeEvent
    public static void onClientTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shakeTicksRemaining > 0) {
            shakeTicksRemaining--;
            if (shakeTicksRemaining < 10) shakeIntensity *= 0.85f;
        }
    }

    // Marker data class
    public static class RocketMarker {
        public final BlockPos target;
        public int ticksRemaining;

        public RocketMarker(BlockPos target, int ticksRemaining) {
            this.target = target;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
