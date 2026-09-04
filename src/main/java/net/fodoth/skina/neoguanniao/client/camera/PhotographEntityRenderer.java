package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.PhotographTextureCache;
import net.fodoth.skina.neoguanniao.content.camera.PhotographEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PhotographEntityRenderer
extends EntityRenderer<PhotographEntity> {
    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath("neoguanniao", "textures/entity/photograph_frame.png");

    public PhotographEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull PhotographEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public void render(@NotNull PhotographEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        float frameSize = 0.75f;
        float photoSize = 0.625f;
        float margin = (frameSize - photoSize) / 2.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entity.getYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float)entity.getRotation() * 90.0f + 180.0f));
        poseStack.translate(-frameSize / 2.0f, -frameSize / 2.0f, 0.026f);
        VertexConsumer photoConsumer = bufferSource.getBuffer(RenderType.text((ResourceLocation)PhotographTextureCache.textureFor(entity.getItem())));
        Matrix4f matrix = poseStack.last().pose();
        PhotographEntityRenderer.renderQuad(photoConsumer, matrix, margin, margin, photoSize, 0.0f, packedLight);
        VertexConsumer frameConsumer = bufferSource.getBuffer(RenderType.text((ResourceLocation)FRAME_TEXTURE));
        PhotographEntityRenderer.renderQuad(frameConsumer, matrix, 0.0f, 0.0f, frameSize, 0.002f, packedLight);
        poseStack.popPose();
    }

    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y, float size, float z, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + size, z, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y + size, z, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y, z, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, 0.0F, 0.0F, packedLight);
    }
}

