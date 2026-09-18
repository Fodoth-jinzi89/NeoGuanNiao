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
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        // 渲染前兜底推进预览时钟：被 Carry On 抱持的鸟不在 level 的 tick 列表里，tickCount 不会自己走，
        // GeckoLib 的动画时钟也就停在原地。世界里正常存在的鸟不会被重复推进。
        if (entity.level() != null) {
            entity.tickPreviewIfNotLevelTicked(entity.level().getGameTime());
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public long getInstanceId(@NotNull T animatable) {
        // 被 Carry On 抱持时每帧都是新建的对象、getId() 也每帧都变：拿它当实例 id，GeckoLib
        // 就会每帧新建 AnimatableManager，动画永远从第 0 帧开始（画出来就是模型默认姿势、看着不动）。
        // 给这类游离副本一个稳定 id（配合 AbstractBirdEntity#getAnimatableInstanceCache 的共享缓存），
        // manager 即可跨帧延续。世界里的鸟在 level 实体表里，identity 判断恒为 false，走原逻辑。
        if (animatable.isRenderCopy()) {
            return 0x5DEECE66DL ^ animatable.getType().hashCode();
        }

        return super.getInstanceId(animatable);
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
        if (animatable.isMale()) {
            shadowRadius *= modelData.maleScale();
            scale *= modelData.maleScale();
        }
        this.shadowRadius = shadowRadius;
        this.withScale(scale);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
}
