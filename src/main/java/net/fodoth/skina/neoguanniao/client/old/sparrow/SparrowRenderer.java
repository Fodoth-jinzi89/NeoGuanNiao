package net.fodoth.skina.neoguanniao.client.old.sparrow;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.SparrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 麻雀渲染器
 * 使用 GeckoLib 渲染麻雀实体
 */
public class SparrowRenderer extends GeoEntityRenderer<SparrowEntity> {

    public SparrowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SparrowModel());
        this.shadowRadius = 0.16F;
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull SparrowEntity animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        float scale = animatable.getModelRenderScale();
        this.withScale(scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}