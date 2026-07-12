package net.fodoth.skina.neoguanniao.client.bath;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor;

import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;


public class BirdBathRenderer extends GeoBlockRenderer<BirdBathBlockEntity> {


    public BirdBathRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(new BirdBathModel());
    }


    @Override
    public void renderRecursively(
            PoseStack poseStack,
            BirdBathBlockEntity birdBath,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {


        BirdBathBoneVisibility.apply(
                birdBath.getContentType(),
                birdBath.getContentLevel(),
                birdBath.getCleanliness(),
                birdBath.getRenderContentType(),
                bone
        );


        float red =
                FastColor.ARGB32.red(colour) / 255.0F;

        float green =
                FastColor.ARGB32.green(colour) / 255.0F;

        float blue =
                FastColor.ARGB32.blue(colour) / 255.0F;

        float alpha =
                FastColor.ARGB32.alpha(colour) / 255.0F;



        if (BirdBathBoneVisibility.isContentBoneVisible(
                birdBath.getContentType(),
                birdBath.getContentLevel(),
                birdBath.getRenderContentType(),
                bone.getName()
        )) {

            float[] tint =
                    BirdBathBoneVisibility.tintFor(
                            birdBath.getContentType(),
                            birdBath.getRenderContentType(),
                            birdBath.getCleanliness()
                    );

            red *= tint[0];
            green *= tint[1];
            blue *= tint[2];


        } else if (BirdBathBoneVisibility.isDirtBone(
                bone.getName()
        )) {

            float[] tint =
                    BirdBathBoneVisibility.dirtTintFor(
                            birdBath.getContentType(),
                            birdBath.getCleanliness(),
                            bone.getName()
                    );

            red *= tint[0];
            green *= tint[1];
            blue *= tint[2];
        }



        colour = FastColor.ARGB32.colorFromFloat(
                alpha,
                red,
                green,
                blue
        );


        super.renderRecursively(
                poseStack,
                birdBath,
                bone,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour
        );
    }



    @Override
    public boolean shouldRenderOffScreen(
            @NotNull BirdBathBlockEntity blockEntity
    ) {
        return true;
    }


    @Override
    public int getViewDistance() {
        return 128;
    }
}