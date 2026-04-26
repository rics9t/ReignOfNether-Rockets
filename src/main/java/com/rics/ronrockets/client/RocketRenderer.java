package com.rics.ronrockets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rics.ronrockets.entity.RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class RocketRenderer extends EntityRenderer<RocketEntity> {

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RocketEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {

        poseStack.pushPose();

        // Tilt toward velocity
        double vx = entity.getDeltaMovement().x;
        double vz = entity.getDeltaMovement().z;

        float angle = (float) Math.atan2(vz, vx);

        poseStack.mulPose(com.mojang.math.Axis.YP.rotation(-angle));

        // Render simple cube
        net.minecraft.client.renderer.entity.EntityRendererProvider.Context context = null;

        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
