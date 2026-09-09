package net.fodoth.skina.neoguanniao.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.neoguanniao.util.TransformUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public abstract class DefaultBlockItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {
    protected final float additionalScale;
    protected final DefaultBlockTransformParams GUI, GROUND, HAND, FIXED, HEAD;

    protected DefaultBlockItemRenderer(GeoModel<T> model) {
        this(model, 1.0F);
    }

    protected DefaultBlockItemRenderer(GeoModel<T> model, float additionalScale) {
        super(model);
        this.additionalScale = additionalScale;
        float inverseScale = 0.5F / additionalScale;
        GUI = new DefaultBlockTransformParams(0.20F * inverseScale, -0.15F * inverseScale, 0F, 0.5F, 45F, 22.5F, 22.5F);
        GROUND = new DefaultBlockTransformParams(0.25F, 0.10F, 0.25F, 0.5F, 0F, 0F, 0F);
        HAND = new DefaultBlockTransformParams(0.20F, 0.0F, 0.0F, 0.5F, 0F, 0F, 0F);
        FIXED = new DefaultBlockTransformParams(0F, -1.125F, 0F, 1F, 0F, -90F, 0F);
        HEAD = new DefaultBlockTransformParams(-0.125F, 0.25F, -0.125F, 1.25F, 0F, 0F, 0F);
    }

    protected final void applyTransform(PoseStack poseStack, DefaultBlockTransformParams params) {
        if (params != null)
            TransformUtil.applyTransformNew(poseStack, params.offsetX(), params.offsetY(), params.offsetZ(), params.scale(), params.rotY(), params.rotX(), params.rotZ());
    }

    protected final DefaultBlockTransformParams transformFor(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> GUI;
            case GROUND -> GROUND;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    HAND;
            case FIXED -> FIXED;
            case HEAD -> HEAD;
            default -> null;
        };
    }

    protected float additionalOffsetY(T item, ItemDisplayContext context) {
        return 0.0F;
    }

    protected float additionalOffsetX(T item, ItemDisplayContext context) {
        return 0.0F;
    }

    protected float additionalOffsetZ(T item, ItemDisplayContext context) {
        return 0.0F;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int colour) {
        if (renderPerspective == ItemDisplayContext.GUI) {
            TransformUtil.applyMinecraftIsometric(poseStack, GUI.scale(), GUI.offsetX(), GUI.offsetY());
        } else {
            applyTransform(poseStack, transformFor(renderPerspective));
        }
        if (renderPerspective == ItemDisplayContext.GUI || renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.scale(additionalScale, additionalScale, additionalScale);
        }
        float offsetX = additionalOffsetX(animatable, renderPerspective);
        if (offsetX != 0.0F) poseStack.translate(offsetX, 0.0F, 0.0F);
        float offsetY = additionalOffsetY(animatable, renderPerspective) / additionalScale;
        if (offsetY != 0.0F) poseStack.translate(0.0F, offsetY, 0.0F);
        float offsetZ = additionalOffsetZ(animatable, renderPerspective) / additionalScale;
        if (offsetZ != 0.0F) poseStack.translate(0.0F, 0.0F, offsetZ);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
    }

}
