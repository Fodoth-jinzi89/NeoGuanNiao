package net.fodoth.skina.neoguanniao.client.nest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestItem;

import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import net.fodoth.skina.neoguanniao.client.DefaultBlockItemRenderer;

public class BirdNestItemRenderer extends DefaultBlockItemRenderer<BirdNestItem> {

    public BirdNestItemRenderer() {
        super(new BirdNestItemModel());
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            BirdNestItem animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}

