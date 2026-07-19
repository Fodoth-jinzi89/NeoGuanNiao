package net.fodoth.skina.neoguanniao.client.bird;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BirdModelRenderer<T extends AbstractBirdEntity<?>> extends GeoEntityRenderer<T> {


    public BirdModelRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BirdModel<>());
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull T animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        var modelData = animatable.getBirdData().model();
        var shadowRadius = modelData.shadowRadius() * modelData.globalScale();
        float scale = animatable.getModelRenderScale() * modelData.globalScale();
        if (animatable.isBaby()) {
            shadowRadius *= modelData.babyScale();
            scale *= modelData.babyScale();
        }
        this.shadowRadius = shadowRadius;
        this.withScale(scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}
