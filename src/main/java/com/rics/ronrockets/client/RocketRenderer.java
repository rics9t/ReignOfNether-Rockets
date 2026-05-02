package com.rics.ronrockets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rics.ronrockets.client.model.RocketModel;
import com.rics.ronrockets.entity.RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RocketRenderer extends EntityRenderer<RocketEntity> {

    private final RocketModel model;

    private static final int FULL_BRIGHT = 0xF000F0;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new RocketModel(context.bakeLayer(RocketLayers.ROCKET_LAYER));
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // The model is built along the +Y axis (nose up).
        // We need to rotate it so the nose points in the direction of travel.
        var delta = entity.getDeltaMovement();
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist > 0.01 || Math.abs(dy) > 0.01) {
            float yaw = (float) Math.atan2(dx, dz);
            float pitch = (float) -Math.atan2(dy, horizontalDist);

            // First rotate to face the horizontal travel direction
            poseStack.mulPose(Axis.YP.rotation(yaw));
            // Then tilt nose up/down based on vertical velocity
            poseStack.mulPose(Axis.XP.rotation(pitch));
        }

        // The model points along +Y; at rest that means nose-up.
        // With the yaw/pitch applied above, it now points along velocity.

        var vertex = buffer.getBuffer(
            net.minecraft.client.renderer.RenderType.entitySolid(getTextureLocation(entity)));

        model.renderToBuffer(poseStack, vertex, FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
            1f, 1f, 1f, 1f);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("ronrockets", "textures/entity/rocket.png");
    }
}
