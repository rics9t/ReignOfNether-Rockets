package com.rics.ronrockets.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rics.ronrockets.entity.RocketEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;

public class RocketModel extends EntityModel<RocketEntity> {

    private final ModelPart rocket;

    public RocketModel(ModelPart root) {
        this.rocket = root.getChild("rocket");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "rocket",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -8.0F, -2.0F,
                                4.0F, 16.0F, 4.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(RocketEntity entity,
                          float limbSwing,
                          float limbSwingAmount,
                          float ageInTicks,
                          float netHeadYaw,
                          float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack,
                               VertexConsumer buffer,
                               int packedLight,
                               int packedOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {

        rocket.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
