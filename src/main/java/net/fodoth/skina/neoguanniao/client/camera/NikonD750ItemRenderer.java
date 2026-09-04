package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.NikonD750ItemModel;
import net.fodoth.skina.neoguanniao.content.camera.NikonD750Item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NikonD750ItemRenderer
extends GeoItemRenderer<NikonD750Item> {
    private static final float MODEL_SCALE = 1.5f;

    public NikonD750ItemRenderer() {
        super((GeoModel)new NikonD750ItemModel());
    }

    public void preRender(PoseStack poseStack, NikonD750Item animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        NikonD750ItemRenderer.applyDisplayTransform(poseStack, this.renderPerspective);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, 0);
    }

    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext context) {
        if (context == ItemDisplayContext.GUI) {
            poseStack.translate(0.19f, -0.01f, 0.0f);
            NikonD750ItemRenderer.scale(poseStack, 0.37f);
        } else if (context == ItemDisplayContext.GROUND) {
            poseStack.translate(0.16f, 0.02f, 0.0f);
            NikonD750ItemRenderer.scale(poseStack, 0.24f);
        } else if (context == ItemDisplayContext.FIXED) {
            poseStack.translate(0.19f, 0.02f, 0.0f);
            NikonD750ItemRenderer.scale(poseStack, 0.3f);
        } else if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.translate(0.1f, 0.16f, 0.18f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-10.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(-8.0f));
            NikonD750ItemRenderer.scale(poseStack, 0.27f);
        } else if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.translate(0.1f, -0.07f, -0.08f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-4.0f));
            NikonD750ItemRenderer.scale(poseStack, 0.5f);
        } else {
            poseStack.translate(0.19f, 0.08f, 0.0f);
            NikonD750ItemRenderer.scale(poseStack, 0.3f);
        }
    }

    private static void scale(PoseStack poseStack, float scale) {
        float finalScale = scale * 1.5f;
        poseStack.scale(finalScale, finalScale, finalScale);
    }
}

