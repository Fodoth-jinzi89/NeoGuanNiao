package net.fodoth.skina.neoguanniao.client.bath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fodoth.skina.neoguanniao.client.DefaultBlockItemRenderer;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathCleanliness;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathItem;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;

public class BirdBathItemRenderer extends DefaultBlockItemRenderer<BirdBathItem> {

    // ============ 构造函数 ============
    public BirdBathItemRenderer() {
        super(new BirdBathItemModel());
    }

    // ============ 渲染方法 ============
    @Override
    public void renderRecursively(PoseStack poseStack, BirdBathItem animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, int colour) {

        BirdBathBoneVisibility.apply(
                BirdBathContentType.EMPTY, 0, BirdBathCleanliness.CLEAN,
                BirdBathContentType.EMPTY, bone
        );

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
    }

}




