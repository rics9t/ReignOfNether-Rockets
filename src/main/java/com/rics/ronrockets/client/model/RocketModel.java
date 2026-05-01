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

PartDefinition bone = partdefinition.addOrReplaceChild("bone",
CubeListBuilder.create()
.texOffs(32, 22).addBox(-3.0F, -47.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
.texOffs(32, 38).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
.texOffs(0, 0).addBox(-4.0F, -37.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(0.0F, 24.0F, 21.0F, 1.5708F, 0.0F, 0.0F));

PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1",
CubeListBuilder.create().texOffs(20, 46)
.addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(-4.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2",
CubeListBuilder.create().texOffs(40, 46)
.addBox(-5.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, -1.5708F, 1.3963F, -1.5708F));

PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3",
CubeListBuilder.create().texOffs(32, 0)
.addBox(-1.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(3.0F, -31.0F, 1.0F, 0.0F, 0.0873F, 0.1745F));

PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4",
CubeListBuilder.create().texOffs(32, 11)
.addBox(-21.0F, -2.0F, -1.0F, 22.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(-3.0F, -31.0F, 1.0F, 0.0F, -0.0873F, -0.1745F));

PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5",
CubeListBuilder.create().texOffs(0, 40)
.addBox(-3.0F, -6.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
PartPose.offsetAndRotation(4.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

return LayerDefinition.create(meshdefinition, 128, 128);
}

@Override
public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
float ageInTicks, float netHeadYaw, float headPitch) {
var delta = entity.getDeltaMovement();
double horizontalDist = Math.hypot(delta.x, delta.z);
if (horizontalDist > 0.01 || delta.y != 0) {
float yaw = (float) (Math.atan2(delta.x, delta.z) * 180 / Math.PI);
float pitch = (float) (-Math.atan2(delta.y, horizontalDist) * 180 / Math.PI);
bone.yRot = (float) Math.toRadians(yaw + 180);
bone.xRot = (float) Math.toRadians(pitch);
}
}

@Override
public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
int packedLight, int packedOverlay,
float red, float green, float blue, float alpha) {
bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
}
}
