package net.fodoth.skina.neoguanniao.client.bird;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BirdModelRenderer<T extends AbstractBirdEntity<?>> extends GeoEntityRenderer<T> {

    private final BirdData birdData;

    public BirdModelRenderer(EntityRendererProvider.Context renderManager, BirdData birdData) {
        super(renderManager, new BirdModel<>());
        this.birdData = birdData;
        this.shadowRadius = birdData.model().shadowRadius();
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull T animatable,
                          @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, int colour) {
        float scale = animatable.getModelRenderScale();
        float finalScale = scale * birdData.model().globalScale();
        this.withScale(finalScale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}
