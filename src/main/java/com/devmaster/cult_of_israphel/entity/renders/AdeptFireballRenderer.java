package com.devmaster.cult_of_israphel.entity.renders;

import com.devmaster.cult_of_israphel.entity.AdeptFireball;
import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AdeptFireballRenderer extends EntityRenderer<AdeptFireball> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Cult_of_Israphel.MOD_ID, "textures/entity/adept_fireball.png");
    private static final int FRAME_COUNT = 8;
    private static final int FRAME_TIME = 4;

    public AdeptFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AdeptFireball entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        int frame = (entity.tickCount / FRAME_TIME) % FRAME_COUNT;
        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = (float) frame / FRAME_COUNT;
        float maxV = (float) (frame + 1) / FRAME_COUNT;
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        vertex(consumer, pose, -0.5F, -0.25F, 0.0F, minU, maxV, packedLight);
        vertex(consumer, pose, 0.5F, -0.25F, 0.0F, maxU, maxV, packedLight);
        vertex(consumer, pose, 0.5F, 0.75F, 0.0F, maxU, minV, packedLight);
        vertex(consumer, pose, -0.5F, 0.75F, 0.0F, minU, minV, packedLight);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {

        consumer.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AdeptFireball entity) {
        return TEXTURE;
    }
}