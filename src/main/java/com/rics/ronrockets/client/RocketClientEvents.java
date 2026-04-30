package com.rics.ronrockets.client;

import com.rics.ronrockets.RonRocketsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
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

    // ── Incoming rocket warnings ──────────────────────────────────
    private static final List<IncomingWarning> INCOMING_WARNINGS = new ArrayList<>();

    private static final int WARNING_DURATION_TICKS = 20 * 8; // 8 seconds

    /**
     * Called when the server broadcasts that a rocket has been launched.
     * Only shows the warning to players who are NOT the attacker.
     */
    public static void onRocketWarningReceived(BlockPos targetPos, String attackerName) {
        LocalPlayer player = MC.player;
        if (player == null) return;
        if (player.getName().getString().equals(attackerName)) return; // don't warn the attacker

        // Play an alarm sound at the player's position
        MC.level.playSound(player, player.blockPosition(), SoundEvents.BELL_RESONATE,
                SoundSource.HOSTILE, 1.0f, 0.6f);

        INCOMING_WARNINGS.add(new IncomingWarning(targetPos, WARNING_DURATION_TICKS));
    }

    // ── Screen shake ──────────────────────────────────────────────
    private static int shakeTicksRemaining = 0;
    private static float shakeIntensity = 0;
    private static BlockPos shakeCenter = null;

    public static void onScreenShake(BlockPos impactPos, float intensity, int durationTicks) {
        // Intensity falls off with distance so far-away players aren't shaken hard
        LocalPlayer player = MC.player;
        if (player != null) {
            double dist = player.distanceToSqr(
                    impactPos.getX() + 0.5, impactPos.getY() + 0.5, impactPos.getZ() + 0.5);
            double falloff = Math.max(0, 1.0 - Math.sqrt(dist) / 200.0);
            intensity *= falloff;
        }
        shakeCenter = impactPos;
        shakeTicksRemaining = durationTicks;
        shakeIntensity = intensity;
    }

    public static float getShakeOffsetX() {
        if (shakeTicksRemaining <= 0) return 0;
        return (MC.level.random.nextFloat() - 0.5f) * shakeIntensity * 2;
    }

    public static float getShakeOffsetY() {
        if (shakeTicksRemaining <= 0) return 0;
        return (MC.level.random.nextFloat() - 0.5f) * shakeIntensity * 2;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (MC.level == null) return;

        // ── Tick incoming warnings ──
        Iterator<IncomingWarning> it = INCOMING_WARNINGS.iterator();
        while (it.hasNext()) {
            IncomingWarning w = it.next();
            w.ticksRemaining--;

            // Blinking red smoke at target
            if (w.ticksRemaining % 3 == 0) {
                MC.level.addParticle(
                        ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        w.target.getX() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 3,
                        w.target.getY() + 8,
                        w.target.getZ() + 0.5 + (MC.level.random.nextDouble() - 0.5) * 3,
                        0, -0.15, 0
                );
            }

            // Falling danger particles closer to impact
            if (w.ticksRemaining < 60 && w.ticksRemaining % 2 == 0) {
                double dx = (MC.level.random.nextDouble() - 0.5) * 1.5;
                double dz = (MC.level.random.nextDouble() - 0.5) * 1.5;
                MC.level.addParticle(
                        ParticleTypes.FLAME,
                        w.target.getX() + 0.5 + dx,
                        w.target.getY() + 15 + MC.level.random.nextDouble() * 10,
                        w.target.getZ() + 0.5 + dz,
                        0, -0.4, 0
                );
            }

            if (w.ticksRemaining <= 0) {
                it.remove();
            }
        }

        // ── Tick screen shake ──
        if (shakeTicksRemaining > 0) {
            shakeTicksRemaining--;
            // Ease out
            if (shakeTicksRemaining < 10) {
                shakeIntensity *= 0.85f;
            }
        }
    }

    // ── Expose for HUD rendering ──────────────────────────────────
    public static List<IncomingWarning> getIncomingWarnings() {
        return INCOMING_WARNINGS;
    }

    public static boolean isScreenShaking() {
        return shakeTicksRemaining > 0;
    }

    // ── Inner data class ──────────────────────────────────────────
    public static class IncomingWarning {
        public final BlockPos target;
        public int ticksRemaining;

        IncomingWarning(BlockPos target, int ticksRemaining) {
            this.target = target;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
