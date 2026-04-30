package com.rics.ronrockets.client;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.util.MyRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Renders pulsing target reticles on the strategic map showing
 * incoming rocket targets (red) and outgoing rocket targets (green).
 * Ring of blocks on the ground + center crosshair.
 */
@Mod.EventBusSubscriber(modid = RonRocketsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketTargetRenderer {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final int RETICLE_RADIUS = 3;
    private static final ResourceLocation WHITE = ResourceLocation.parse("forge:textures/white.png");

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!OrthoviewClientEvents.isEnabled()) return;
        if (MC.level == null) return;

        List<RocketClientEvents.RocketMarker> incoming = RocketClientEvents.getIncomingWarnings();
        List<RocketClientEvents.RocketMarker> outgoing = RocketClientEvents.getOutgoingMarkers();

        if (incoming.isEmpty() && outgoing.isEmpty()) return;

        VertexConsumer vc = MC.renderBuffers().bufferSource()
                .getBuffer(RenderType.entityTranslucent(WHITE));
        PoseStack poseStack = evt.getPoseStack();

        // Pulsing alpha
        float alpha = 0.3f + 0.5f * (0.5f + 0.5f * (float) Math.sin(
                MC.level.getGameTime() * 0.3));

        // ── Incoming threats — red ────────────────────────────
        for (RocketClientEvents.RocketMarker w : incoming) {
            drawReticle(poseStack, vc, w.target, 1.0f, 0.2f, 0.2f, alpha);
        }

        // ── Outgoing strikes — green ──────────────────────────
        for (RocketClientEvents.RocketMarker m : outgoing) {
            drawReticle(poseStack, vc, m.target, 0.2f, 1.0f, 0.2f, alpha);
        }
    }

    private static void drawReticle(PoseStack poseStack, VertexConsumer vc,
                                     BlockPos center, float r, float g, float b, float a) {
        // Ring around target
        for (int dx = -RETICLE_RADIUS; dx <= RETICLE_RADIUS; dx++) {
            for (int dz = -RETICLE_RADIUS; dz <= RETICLE_RADIUS; dz++) {
                if (Math.abs(dx) + Math.abs(dz) != RETICLE_RADIUS) continue;
                MyRenderer.drawBlockFace(poseStack, vc, Direction.UP, 0,
                        center.offset(dx, 0, dz), r, g, b, a);
            }
        }

        // Center crosshair (slightly brighter)
        for (int i = -1; i <= 1; i++) {
            MyRenderer.drawBlockFace(poseStack, vc, Direction.UP, 0,
                    center.offset(i, 0, 0), r, g, b, a * 1.2f);
            MyRenderer.drawBlockFace(poseStack, vc, Direction.UP, 0,
                    center.offset(0, 0, i), r, g, b, a * 1.2f);
        }
    }
}
