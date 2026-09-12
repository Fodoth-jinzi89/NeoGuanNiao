package net.fodoth.skina.neoguanniao.client.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class PhotographItemRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation CARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "textures/item/photograph_card.png");

    public PhotographItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        try {
            PhotographItemRenderer.applyDisplayTransform(poseStack, context);
            int light = context == ItemDisplayContext.GUI ? 0xF000F0 : packedLight;
            Matrix4f matrix = poseStack.last().pose();
            ResourceLocation photoTexture = PhotographData.hasImage(stack)
                    ? PhotographTextureCache.textureFor(stack) : CARD_TEXTURE;
            VertexConsumer cardConsumer = bufferSource.getBuffer(RenderType.text(photoTexture));
            PhotographItemRenderer.renderPhoto(cardConsumer, matrix, light);
            Block block = BuiltInRegistries.BLOCK.get(PhotographData.frameBlock(stack));
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(block.defaultBlockState());
            VertexConsumer frameConsumer = bufferSource.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));
            PhotographItemRenderer.renderFrame(frameConsumer, matrix, sprite, light);
        }
        finally {
            poseStack.popPose();
        }
    }

    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext context) {
        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.0f, 0.0f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0f));
            poseStack.scale(0.96f, 0.96f, 0.96f);
        } else if (context == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0f, 0.05f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.scale(0.62f, 0.62f, 0.62f);
        } else if (context == ItemDisplayContext.FIXED) {
            poseStack.scale(0.78f, 0.78f, 0.78f);
        } else {
            poseStack.translate(0.0f, 0.02f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-6.0f));
            poseStack.scale(0.72f, 0.72f, 0.72f);
        }
    }

    private static void renderPhoto(VertexConsumer consumer, Matrix4f matrix, int packedLight) {
        renderRect(consumer, matrix, -0.41F, -0.31F, 0.82F, 0.62F, 0.0F,
                0.0F, 1.0F, 0.0F, 1.0F, packedLight);
    }

    private static void renderFrame(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite, int light) {
        float x = -0.45F, y = -0.35F, width = 0.9F, height = 0.7F, border = 0.07F;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        float du = (u1 - u0) * 0.125F, dv = (v1 - v0) * 0.125F;
        renderRect(consumer, matrix, x, y + height - border, width, border, -0.002F, u0, u1, v0, v0 + dv, light);
        renderRect(consumer, matrix, x, y, width, border, -0.002F, u0, u1, v1 - dv, v1, light);
        renderRect(consumer, matrix, x, y + border, border, height - border * 2, -0.002F, u0, u0 + du, v0, v1, light);
        renderRect(consumer, matrix, x + width - border, y + border, border, height - border * 2, -0.002F, u1 - du, u1, v0, v1, light);
    }

    private static void renderRect(VertexConsumer consumer, Matrix4f matrix, float x, float y,
                                   float width, float height, float z, float u0, float u1,
                                   float v0, float v1, int light) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + height, z, u0, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y + height, z, u1, v1, light);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y, z, u1, v0, light);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, u0, v0, light);
    }
}

