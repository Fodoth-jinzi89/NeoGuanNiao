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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PhotographEntityRenderer
extends EntityRenderer<PhotographEntity> {

    /** 相框整体离方块外沿的留白（像素）。 */
    private static final float FRAME_GAP_PIXELS = 2.0f;
    /** 相框边框厚度（像素）。 */
    private static final float FRAME_BORDER_PIXELS = 2.0f;
    /** 1x1 相框的边框厚度（像素），比其它尺寸细 1 像素。 */
    private static final float SMALL_FRAME_BORDER_PIXELS = 1.0f;
    private static final float FRAME_TEXTURE_SIZE = 16.0f;
    private static final float EPSILON = 1.0E-4f;

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
        // 边长（格）：1x1 ~ 8x8，与实体碰撞箱一致；每格 16 像素。
        int size = PhotographData.frameSize(entity.getItem());
        float gap = FRAME_GAP_PIXELS / FRAME_TEXTURE_SIZE;
        // 1x1 的边框只有 1 像素，照片相应外扩 1 像素。
        float border = (size == 1 ? SMALL_FRAME_BORDER_PIXELS : FRAME_BORDER_PIXELS) / FRAME_TEXTURE_SIZE;
        float photoSize = (float) size - (gap + border) * 2.0f;
        float photoMin = gap + border;
        float photoCenter = photoMin + photoSize / 2.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entity.getYRot()));
        poseStack.translate(-(float) size / 2.0f, -(float) size / 2.0f, 0.026f);
        Matrix4f matrix = poseStack.last().pose();
        var block = BuiltInRegistries.BLOCK.get(PhotographData.frameBlock(entity.getItem()));
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(block.defaultBlockState()).getParticleIcon();
        VertexConsumer frameConsumer = bufferSource.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));
        PhotographEntityRenderer.renderFrame(frameConsumer, matrix, size, gap, border, packedLight, sprite);
        // 右键旋转只作用于照片本体，相框保持竖直不动。
        poseStack.pushPose();
        poseStack.translate(photoCenter, photoCenter, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float)entity.getRotation() * 90.0f + 180.0f));
        poseStack.translate(-photoCenter, -photoCenter, 0.0f);
        VertexConsumer photoConsumer = bufferSource.getBuffer(RenderType.text(PhotographTextureCache.textureFor(entity.getItem())));
        PhotographEntityRenderer.renderQuad(photoConsumer, poseStack.last().pose(), photoMin, photoMin, photoSize, -0.002f, packedLight);
        poseStack.popPose();
        poseStack.popPose();
    }

    /**
     * 画相框四边：边框厚 {@code border} 格（1x1 为 1 像素、其余为 {@link #FRAME_BORDER_PIXELS} 像素），
     * 纹理沿边长按“一格一张方块纹理”平铺，边长几格就平铺几张
     * （边框总长不是整数格时按剩余长度裁剪 UV，不拉伸纹理）。
     */
    private static void renderFrame(VertexConsumer consumer, Matrix4f matrix, int size, float gap, float border, int light, TextureAtlasSprite sprite) {
        float max = size - gap;
        float edge = max - border;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float uBorder = (u1 - u0) * border;
        float vBorder = (v1 - v0) * border;
        for (float x = gap; x < max - EPSILON; ) {
            float width = Math.min(1.0f, max - x);
            float uEnd = u0 + (u1 - u0) * width;
            PhotographEntityRenderer.renderSprite(consumer, matrix, x, edge, width, border, 0.002f, light, u0, uEnd, v0, v0 + vBorder);
            PhotographEntityRenderer.renderSprite(consumer, matrix, x, gap, width, border, 0.002f, light, u0, uEnd, v1 - vBorder, v1);
            x += width;
        }
        float verticalMin = gap + border;
        float verticalMax = max - border;
        for (float y = verticalMin; y < verticalMax - EPSILON; ) {
            float height = Math.min(1.0f, verticalMax - y);
            float vEnd = v0 + (v1 - v0) * height;
            PhotographEntityRenderer.renderSprite(consumer, matrix, gap, y, border, height, 0.002f, light, u0, u0 + uBorder, v0, vEnd);
            PhotographEntityRenderer.renderSprite(consumer, matrix, edge, y, border, height, 0.002f, light, u1 - uBorder, u1, v0, vEnd);
            y += height;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y, float size, float z, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + size, z, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y + size, z, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + size, y, z, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, 0.0F, 0.0F, packedLight);
    }

    @SuppressWarnings("SameParameterValue")
    private static void renderSprite(VertexConsumer consumer, Matrix4f matrix, float x, float y, float width, float height, float z, int light, float u0, float u1, float v0, float v1) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + height, z, u0, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y + height, z, u1, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y, z, u1, v0, light);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, u0, v0, light);
    }
}

