package net.fodoth.skina.neoguanniao.client.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class FilmItemRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation FILM_TEXTURE = ResourceLocation.fromNamespaceAndPath("neoguanniao", "textures/item/film.png");
    private static final float FILM_WIDTH = 0.78f;
    private static final float FILM_HEIGHT = 0.54f;

    public FilmItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        try {
            FilmItemRenderer.applyDisplayTransform(poseStack, context);
            int light = context == ItemDisplayContext.GUI ? 0xF000F0 : packedLight;
            Matrix4f matrix = poseStack.last().pose();
            VertexConsumer filmConsumer = bufferSource.getBuffer(RenderType.text((ResourceLocation)FILM_TEXTURE));
            FilmItemRenderer.renderQuad(filmConsumer, matrix, -0.39f, -0.27f, 0.78f, 0.54f, 0.0f, light);
        }
        finally {
            poseStack.popPose();
        }
    }

    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext context) {
        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.0f, 0.0f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-6.0f));
            poseStack.scale(0.96f, 0.96f, 0.96f);
        } else if (context == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0f, 0.05f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.scale(0.58f, 0.58f, 0.58f);
        } else if (context == ItemDisplayContext.FIXED) {
            poseStack.scale(0.76f, 0.76f, 0.76f);
        } else {
            poseStack.translate(0.0f, 0.02f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-5.0f));
            poseStack.scale(0.68f, 0.68f, 0.68f);
        }
    }

    private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, float x, float y, float width, float height, float z, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, x, y + height, z, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y + height, z, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x + width, y, z, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, x, y, z, 0.0F, 0.0F, packedLight);
    }
}

