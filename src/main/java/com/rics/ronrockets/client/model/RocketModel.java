package com.rics.ronrockets.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rics.ronrockets.entity.RocketEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class RocketModel extends EntityModel<RocketEntity> {

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

        // ── Fuselage ──
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        // Nose cone
                        .texOffs(32, 22).addBox(-3.0F, -47.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                        // Main body
                        .texOffs(0, 0).addBox(-4.0F, -37.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F))
                        // Nozzle ring
                        .texOffs(32, 38).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 24.0F, 25.0F, 1.5708F, 0.0F, 0.0F));

        // ── Fins ──
        // Rear dorsal fin
        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(40, 46)
                        .addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, -1.5708F, 1.3963F, -1.5708F));

        // Right wing
        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -31.0F, 1.0F, 0.0F, 0.0873F, 0.1745F));

        // Right finlet
        bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-3.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        // Left finlet
        bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create().texOffs(20, 46)
                        .addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        // Left wing
        bone.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(32, 11)
                        .addBox(-21.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -31.0F, 1.0F, 0.0F, -0.0873F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(RocketEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
