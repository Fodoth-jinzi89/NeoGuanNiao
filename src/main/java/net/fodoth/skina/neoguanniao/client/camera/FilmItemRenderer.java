package net.fodoth.skina.neoguanniao.client.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class FilmItemRenderer
extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation FILM_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "textures/item/film.png");

    public FilmItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        try {
            FilmItemRenderer.applyDisplayTransform(poseStack, context);
            int light = context == ItemDisplayContext.GUI ? 0xF000F0 : packedLight;
            Matrix4f matrix = poseStack.last().pose();
            VertexConsumer filmConsumer = bufferSource.getBuffer(RenderType.text(FILM_TEXTURE));
            FilmItemRenderer.renderQuad(filmConsumer, matrix, light);
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

        private static void renderQuad(VertexConsumer consumer, Matrix4f matrix, int packedLight) {
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.39, (float) -0.27 + (float) 0.54, (float) 0.0, 0.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.39 + (float) 0.78, (float) -0.27 + (float) 0.54, (float) 0.0, 1.0F, 1.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.39 + (float) 0.78, (float) -0.27, (float) 0.0, 1.0F, 0.0F, packedLight);
        CameraRenderUtil.vertex(consumer, matrix, (float) -0.39, (float) -0.27, (float) 0.0, 0.0F, 0.0F, packedLight);
    }
}

