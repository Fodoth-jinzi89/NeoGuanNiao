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
    public BirdModelRenderer(EntityRendererProvider.Context context) { super(context, new BirdModel<>()); }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull T bird, @NotNull BakedGeoModel model,
                          @Nullable MultiBufferSource buffers, @Nullable VertexConsumer buffer, boolean reRender,
                          float partialTick, int light, int overlay, int color) {
        var data = bird.getBirdData().model();
        float scale = bird.getModelRenderScale() * data.globalScale();
        if (bird.isBaby()) scale *= data.babyScale();
        if (bird.isMale()) scale *= data.maleScale();
        this.shadowRadius = data.shadowRadius() * data.globalScale();
        this.withScale(scale);
        super.preRender(poseStack, bird, model, buffers, buffer, reRender, partialTick, light, overlay, color);
    }
}
