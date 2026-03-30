package com.cockatielmod.cockatiel.client;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CockatielModel extends EntityModel<CockatielEntity> {

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart crest;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public CockatielModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.beak = head.getChild("beak");
        this.crest = head.getChild("crest");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.tail = body.getChild("tail");
        this.leftLeg = body.getChild("left_leg");
        this.rightLeg = body.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0f, -3.0f, -4.0f, 6, 6, 8),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        // Head
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 0)
                        .addBox(-2.5f, -5.0f, -2.5f, 5, 5, 5),
                PartPose.offset(0.0f, -3.0f, -2.0f));

        // Beak - upper
        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-1.0f, -1.5f, -3.5f, 2, 2, 2),
                PartPose.ZERO);

        // Crest feathers
        head.addOrReplaceChild("crest",
                CubeListBuilder.create().texOffs(8, 14)
                        .addBox(-0.5f, -8.0f, -1.0f, 1, 4, 1)
                        .addBox(-1.0f, -7.5f, -0.5f, 1, 3, 1)
                        .addBox(0.5f, -7.5f, -0.5f, 1, 3, 1),
                PartPose.ZERO);

        // Left wing
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(28, 10)
                        .addBox(0.0f, -1.0f, -3.0f, 1, 4, 7),
                PartPose.offset(3.0f, -1.0f, 1.0f));

        // Right wing
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(28, 10)
                        .addBox(-1.0f, -1.0f, -3.0f, 1, 4, 7),
                PartPose.offset(-3.0f, -1.0f, 1.0f));

        // Tail
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-1.5f, 0.0f, 0.0f, 3, 1, 5),
                PartPose.offsetAndRotation(0.0f, 0.0f, 4.0f,
                        0.3f, 0.0f, 0.0f));

        // Left leg
        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(20, 22)
                        .addBox(-0.5f, 0.0f, -1.0f, 1, 3, 1),
                PartPose.offset(1.5f, 3.0f, -1.0f));

        // Right leg
        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(24, 22)
                        .addBox(-0.5f, 0.0f, -1.0f, 1, 3, 1),
                PartPose.offset(-1.5f, 3.0f, -1.0f));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(CockatielEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Head look
        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;

        // Crest bobbing when singing or alerting
        if (entity.isSinging() || entity.isAlerting()) {
            this.crest.xRot = Mth.sin(ageInTicks * 0.3f) * 0.3f;
        } else {
            this.crest.xRot = 0.0f;
        }

        // Wing flap when flying
        if (entity.isFlying()) {
            float flapSpeed = 1.5f;
            float flapAmount = 0.8f;
            this.leftWing.zRot = Mth.sin(ageInTicks * flapSpeed) * flapAmount;
            this.rightWing.zRot = -Mth.sin(ageInTicks * flapSpeed) * flapAmount;
            this.leftWing.xRot = Mth.sin(ageInTicks * flapSpeed * 0.5f) * 0.2f;
            this.rightWing.xRot = Mth.sin(ageInTicks * flapSpeed * 0.5f) * 0.2f;
        } else {
            // Idle wing flutter - very subtle
            this.leftWing.zRot = Mth.sin(ageInTicks * 0.05f) * 0.05f;
            this.rightWing.zRot = -Mth.sin(ageInTicks * 0.05f) * 0.05f;
            this.leftWing.xRot = 0.0f;
            this.rightWing.xRot = 0.0f;
        }

        // Tail sway
        this.tail.zRot = Mth.sin(ageInTicks * 0.07f) * 0.05f;

        // Leg swing while walking
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6f) * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6f + Mth.PI) * limbSwingAmount;

        // Alert pose - head up, crest raised
        if (entity.isAlerting()) {
            this.head.xRot = -0.3f;
            this.crest.xRot = -0.4f;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
