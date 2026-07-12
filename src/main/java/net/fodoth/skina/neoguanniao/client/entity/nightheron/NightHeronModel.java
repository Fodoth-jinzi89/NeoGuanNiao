package net.fodoth.skina.neoguanniao.client.entity.nightheron;

import net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronDefinition;
import net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

/**
 * 夜鹭模型
 * 使用 GeckoLib 加载夜鹭的模型、纹理和动画
 */
public class NightHeronModel extends GeoModel<NightHeronEntity> {

    public NightHeronModel() {
    }

    @Override
    public @NotNull ResourceLocation getModelResource(@NotNull NightHeronEntity animatable) {
        return NightHeronDefinition.MODEL;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(@NotNull NightHeronEntity animatable) {
        return NightHeronDefinition.TEXTURE;
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(@NotNull NightHeronEntity animatable) {
        return NightHeronDefinition.ANIMATION;
    }
}