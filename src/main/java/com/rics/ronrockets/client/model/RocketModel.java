package com.rics.ronrockets.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rics.ronrockets.entity.RocketEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class RocketModel<T extends RocketEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("ronrockets", "rocket"),
            "main"
        );

    private final ModelPart bone;

    public RocketModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Rocket oriented along +Y axis (nose pointing up).
        // Origin at (0, 24, 0) = ground level in model space.
        // Body: 8x32x8 box centered on X/Z, extending upward from Y=0 to Y=32
        // Nose cone: 6x10x6 on top of body (Y=32 to Y=42)
        // Nozzle: 6x2x6 below body (Y=-2 to Y=0)
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
            CubeListBuilder.create()
                // Nose cone (top)
                .texOffs(32, 22).addBox(-3.0F, -42.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                // Nozzle (bottom)
                .texOffs(32, 38).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                // Body (middle)
                .texOffs(0, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        // Left fin
        bone.addOrReplaceChild("cube_r1",
            CubeListBuilder.create().texOffs(20, 46)
                .addBox(-3.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        // Right fin
        bone.addOrReplaceChild("cube_r5",
            CubeListBuilder.create().texOffs(0, 40)
                .addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        // Front fin
        bone.addOrReplaceChild("cube_r2",
            CubeListBuilder.create().texOffs(40, 46)
                .addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -1.5708F, 0.0F, 0.0F));

        // Left wing
        bone.addOrReplaceChild("cube_r3",
            CubeListBuilder.create().texOffs(32, 0)
                .addBox(-1.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(3.0F, -26.0F, 0.0F, 0.0F, 0.0873F, 0.1745F));

        // Right wing
        bone.addOrReplaceChild("cube_r4",
            CubeListBuilder.create().texOffs(32, 11)
                .addBox(-21.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-3.0F, -26.0F, 0.0F, 0.0F, -0.0873F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Orientation is handled entirely in RocketRenderer via PoseStack rotations
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
