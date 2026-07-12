package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;


public class BirdCageItemRenderer extends GeoItemRenderer<BirdCageItem> {


    public BirdCageItemRenderer() {
        super(new BirdCageItemModel());
    }


    @Override
    public void preRender(
            PoseStack poseStack,
            BirdCageItem animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {

        BirdCageVariant variant = animatable.variant();

        poseStack.translate(
                itemOffsetX(variant),
                itemOffsetY(variant),
                0.0F
        );

        float scale = itemScale(variant);

        poseStack.scale(
                scale,
                scale,
                scale
        );


        super.preRender(
                poseStack,
                animatable,
                model,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour
        );
    }



    private static float itemScale(BirdCageVariant variant) {

        return switch (variant) {
            case SMALL -> 0.78F;
            case MEDIUM -> 0.3F;
            case LARGE -> 0.22F;
        };
    }



    private static float itemOffsetX(BirdCageVariant variant) {

        return switch (variant) {
            case SMALL -> 0.08F;
            case MEDIUM -> 0.3F;
            case LARGE -> 0.37F;
        };
    }



    private static float itemOffsetY(BirdCageVariant variant) {

        return switch (variant) {
            case SMALL -> -0.4F;
            case MEDIUM, LARGE -> -0.1F;
        };
    }
}