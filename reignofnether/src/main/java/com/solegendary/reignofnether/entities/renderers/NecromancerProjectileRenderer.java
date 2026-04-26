//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.solegendary.reignofnether.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.solegendary.reignofnether.entities.NecromancerProjectile;
import com.solegendary.reignofnether.entities.models.NecromancerProjectileModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NecromancerProjectileRenderer extends EntityRenderer<NecromancerProjectile> {

    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("reignofnether", "textures/entities/necromancer_projectile.png");

    private static final RenderType RENDER_TYPE;
    private final NecromancerProjectileModel<NecromancerProjectile> model;

    public NecromancerProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new NecromancerProjectileModel<>(pContext.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    protected int getBlockLightLevel(NecromancerProjectile pEntity, BlockPos pPos) {
        return 0;
    }

    public void render(NecromancerProjectile pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        float $$6 = Mth.rotLerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot());
        float $$7 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
        float $$8 = (float)pEntity.tickCount + pPartialTicks;
        pPoseStack.translate(0.0F, 0.15F, 0.0F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin($$8 * 0.1F) * 180.0F));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos($$8 * 0.1F) * 180.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin($$8 * 0.15F) * 360.0F));
        pPoseStack.scale(-0.5F, -0.5F, 0.5F);
        this.model.setupAnim(pEntity, 0.0F, 0.0F, 0.0F, $$6, $$7);
        VertexConsumer $$9 = pBuffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(pPoseStack, $$9, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pPoseStack.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer $$10 = pBuffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(pPoseStack, $$10, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public ResourceLocation getTextureLocation(NecromancerProjectile pEntity) {
        return TEXTURE_LOCATION;
    }

    static {
        RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    }
}
