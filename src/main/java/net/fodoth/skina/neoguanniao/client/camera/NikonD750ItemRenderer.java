package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.content.camera.NikonD750Item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.fodoth.skina.neoguanniao.util.TransformUtil;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NikonD750ItemRenderer
extends GeoItemRenderer<NikonD750Item> {
    public NikonD750ItemRenderer() {
        super(new NikonD750ItemModel());
    }

    @Override
    public void preRender(PoseStack poseStack, NikonD750Item animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        NikonD750ItemRenderer.applyDisplayTransform(poseStack, this.renderPerspective);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    private static void applyDisplayTransform(PoseStack poseStack, ItemDisplayContext context) {
        switch (context) {
            case GUI -> {
                TransformUtil.applyTransform(poseStack, -11.5f / 16f, -5.0f / 16f, 0.0f, 0.8f, 135.0f, -5.0f, 5.0f);
            }
            case GROUND -> {
                TransformUtil.applyTransform(poseStack, 0.3f, 1.5f / 16f, 0.3f, 0.5f, 0.0f, 0.0f, 0.0f);
            }
            case FIXED -> poseStack.translate(0.0f, 0.0f, -2.75f / 16f);
            case HEAD -> {
                TransformUtil.applyTransform(poseStack, 0.0f, 9.5f / 16f, -6.75f / 16f, 2.0f, 0.0f, 0.0f, 0.0f);
            }
            // First-person transforms are intentionally kept in the camera-specific renderer.
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                TransformUtil.applyTransform(poseStack, 0.0f, 4.75f / 16f, 0.0f, 0.6f, 0.0f, 0.0f, 0.0f);
            }
            default -> {
            }
        }
    }
}

