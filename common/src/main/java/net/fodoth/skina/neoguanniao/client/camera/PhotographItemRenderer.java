package net.fodoth.skina.neoguanniao.client.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
    private static final ResourceLocation CARD_TEXTURE = ResourceLocation.fromNamespaceAndPath("neoguanniao", "textures/item/photograph_card.png");
    private static final float CARD_WIDTH = 0.82f;
    private static final float CARD_HEIGHT = 0.62f;

    public PhotographItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SuppressWarnings("deprecation")
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        try {
            PhotographItemRenderer.applyDisplayTransform(poseStack, context);
            int light = context == ItemDisplayContext.GUI ? 0xF000F0 : packedLight;
            Matrix4f matrix = poseStack.last().pose();
            VertexConsumer cardConsumer = bufferSource.getBuffer(RenderType.text(CARD_TEXTURE));
            PhotographItemRenderer.renderQuad(cardConsumer, matrix, light);
            Block block = BuiltInRegistries.BLOCK.get(PhotographData.frameBlock(stack));
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(block.defaultBlockState());
            VertexConsumer frameConsumer = bufferSource.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));
            PhotographItemRenderer.renderSprite(frameConsumer, matrix, sprite, light);
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

    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.41, (float) -0.31 + (float) 0.62, (float) 0.0, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.41 + (float) 0.82, (float) -0.31 + (float) 0.62, (float) 0.0, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.41 + (float) 0.82, (float) -0.31, (float) 0.0, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.41, (float) -0.31, (float) 0.0, 0.0F, 0.0F, packedLight);
    }

    private static void renderSprite(VertexConsumer consumer, Matrix4f matrix, TextureAtlasSprite sprite, int light) {
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.45, (float) -0.35 + (float) 0.7, (float) -0.002, sprite.getU0(), sprite.getV1(), light);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.45 + (float) 0.9, (float) -0.35 + (float) 0.7, (float) -0.002, sprite.getU1(), sprite.getV1(), light);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.45 + (float) 0.9, (float) -0.35, (float) -0.002, sprite.getU1(), sprite.getV0(), light);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.45, (float) -0.35, (float) -0.002, sprite.getU0(), sprite.getV0(), light);
    }
}

