package net.fodoth.skina.neoguanniao.client.camera;
import net.fodoth.skina.neoguanniao.content.camera.PhotographEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PhotographEntityRenderer
extends EntityRenderer<PhotographEntity> {

    /** Entity-space size of the hanging frame. */
    private static final float FRAME_SIZE = 0.75f;
    /** A block sprite is 16 pixels wide; keep exactly one pixel as the border. */
    private static final float FRAME_BORDER_PIXELS = 1.0f;
    private static final float FRAME_TEXTURE_SIZE = 16.0f;

    public PhotographEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull PhotographEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(@NotNull PhotographEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        float border = FRAME_SIZE * FRAME_BORDER_PIXELS / FRAME_TEXTURE_SIZE;
        float photoSize = FRAME_SIZE - border * 2.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entity.getYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float)entity.getRotation() * 90.0f + 180.0f));
        poseStack.translate(-FRAME_SIZE / 2.0f, -FRAME_SIZE / 2.0f, 0.026f);
        Matrix4f matrix = poseStack.last().pose();
        var block = BuiltInRegistries.BLOCK.get(PhotographData.frameBlock(entity.getItem()));
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(block.defaultBlockState());
        VertexConsumer frameConsumer = bufferSource.getBuffer(RenderType.text(TextureAtlas.LOCATION_BLOCKS));
        // Render only the four opaque border strips, so the frame can never occlude the photo.
        // The whole frame is 16x16; the photo covers the central 14x14, leaving the outer 1px border.
        float pixel = (sprite.getU1() - sprite.getU0()) / FRAME_TEXTURE_SIZE;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float innerU0 = u0 + pixel;
        float innerU1 = u1 - pixel;
        float innerV0 = v0 + pixel;
        float innerV1 = v1 - pixel;
        PhotographEntityRenderer.renderSprite(frameConsumer, matrix, sprite, 0.0f, 0.0f, FRAME_SIZE, border, 0.002f, packedLight, u0, u1, v0, v0 + pixel);
        PhotographEntityRenderer.renderSprite(frameConsumer, matrix, sprite, 0.0f, FRAME_SIZE - border, FRAME_SIZE, border, 0.002f, packedLight, u0, u1, v1 - pixel, v1);
        PhotographEntityRenderer.renderSprite(frameConsumer, matrix, sprite, 0.0f, border, border, photoSize, 0.002f, packedLight, u0, innerU0, innerV0, innerV1);
        PhotographEntityRenderer.renderSprite(frameConsumer, matrix, sprite, FRAME_SIZE - border, border, border, photoSize, 0.002f, packedLight, innerU1, u1, innerV0, innerV1);
        VertexConsumer photoConsumer = bufferSource.getBuffer(RenderType.text(PhotographTextureCache.textureFor(entity.getItem())));
        PhotographEntityRenderer.renderQuad(photoConsumer, matrix, border, border, photoSize, 0.004f, packedLight);
        poseStack.popPose();
    }

    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y, float size, float z, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + size, z, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y + size, z, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y, z, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, 0.0F, 0.0F, packedLight);
    }

    private static void renderSprite(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite, float x, float y, float width, float height, float z, int light) {
        renderSprite(consumer, matrix, sprite, x, y, width, height, z, light, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    private static void renderSprite(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite, float x, float y, float width, float height, float z, int light, float u0, float u1, float v0, float v1) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + height, z, u0, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y + height, z, u1, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y, z, u1, v0, light);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, u0, v0, light);
    }
}

