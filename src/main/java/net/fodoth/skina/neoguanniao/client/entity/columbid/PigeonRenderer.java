package net.fodoth.skina.neoguanniao.client.entity.columbid;

import net.fodoth.skina.neoguanniao.content.bird.columbid.PigeonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 鸽子渲染器
 * 使用 GeckoLib 渲染鸽子实体
 */
public class PigeonRenderer extends GeoEntityRenderer<PigeonEntity> {

    public PigeonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ColumbidModel<>());
        this.shadowRadius = 0.28F;
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull PigeonEntity animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        this.withScale(animatable.getModelRenderScale());
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}