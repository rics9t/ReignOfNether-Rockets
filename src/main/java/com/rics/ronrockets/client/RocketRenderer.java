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

    // Full-bright light value (15 sky + 15 block = 0xF0F0)
    private static final int FULL_BRIGHT = 0xF000F0;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new RocketModel(context.bakeLayer(RocketLayers.ROCKET_LAYER));
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        double vx = entity.getDeltaMovement().x;
        double vz = entity.getDeltaMovement().z;
        double vy = entity.getDeltaMovement().y;

        float yaw = (float) Math.atan2(vz, vx);
        float pitch = (float) Math.atan2(vy, Math.sqrt(vx * vx + vz * vz));

        poseStack.mulPose(Axis.YP.rotation(-yaw));
        poseStack.mulPose(Axis.ZP.rotation(pitch));

        // Use full-bright so the rocket is always visible, even at night or in shadows
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
