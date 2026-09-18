package net.fodoth.skina.neoguanniao.client.bird;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BirdModel<T extends AbstractBirdEntity<?>> extends GeoModel<T> {

    public BirdModel() {
    }

    @Override
    public @NotNull ResourceLocation getModelResource(@NotNull T animatable) {
        return animatable.getModelResource();
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(@NotNull T animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(@NotNull T animatable) {
        var modelAnimationMap = animatable.getBirdData().animation().modelAnimationMap();
        ResourceLocation animation = modelAnimationMap.get(animatable.getModelId());

        // 兜底：被 Carry On 抱持 / 别的模组复原出来的实体，同步数据可能还没到、模型变体查不到对应条目，
        // 这时返回 null 会让 GeckoLib 因为拿不到动画文件而完全跳过骨骼变换（模型僵在预置姿势、一动不动）。
        // 同一份 BirdData 的模型动画表是按物种给的，取第一条即可。
        if (animation == null) {
            animation = modelAnimationMap.values().stream().findFirst().orElse(null);
        }

        return animation;
    }

    @Override
    public void handleAnimations(@NotNull T animatable, long instanceId,
                                 @NotNull AnimationState<T> animationState, float partialTick) {
        // 兜底驱动：被 Carry On 抱持的鸟不在 level 的 tick 列表里，tickCount 不会自己走，
        // GeckoLib 的动画时钟（currentFrameTime = tickCount + partialTick）也就停在原地。
        // handleAnimations 是每帧渲染必经之处，在这里补一次时钟（世界里正常存在的鸟不会被推进）。
        if (animatable.level() != null) {
            animatable.tickPreviewIfNotLevelTicked(animatable.level().getGameTime());
        }

        super.handleAnimations(animatable, instanceId, animationState, partialTick);
    }
}
