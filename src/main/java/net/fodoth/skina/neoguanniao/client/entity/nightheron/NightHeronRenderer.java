package net.fodoth.skina.neoguanniao.client.entity.nightheron;

import net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 夜鹭渲染器
 * 使用 GeckoLib 渲染夜鹭实体
 */
public class NightHeronRenderer extends GeoEntityRenderer<NightHeronEntity> {

    public NightHeronRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NightHeronModel());
        this.addRenderLayer(new NightHeronHeldFishLayer(this));
        this.shadowRadius = 0.45F;
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull NightHeronEntity animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        float scale = animatable.getModelRenderScale();
        this.withScale(scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}