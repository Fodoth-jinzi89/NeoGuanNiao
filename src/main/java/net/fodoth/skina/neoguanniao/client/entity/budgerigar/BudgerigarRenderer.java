package net.fodoth.skina.neoguanniao.client.entity.budgerigar;

import net.fodoth.skina.neoguanniao.content.bird.impl.budgerigar.BudgerigarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 虎皮鹦鹉渲染器
 * 使用 GeckoLib 渲染虎皮鹦鹉实体
 */
public class BudgerigarRenderer extends GeoEntityRenderer<BudgerigarEntity> {

    public BudgerigarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BudgerigarModel());
        this.shadowRadius = 0.12F;
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull BudgerigarEntity animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        float scale = animatable.getModelRenderScale();
        this.withScale(scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}