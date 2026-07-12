package net.fodoth.skina.neoguanniao.client.entity.columbid;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

/**
 * 鸽形目鸟类模型
 * 使用 GeckoLib 加载鸽形目鸟类的模型、纹理和动画
 */
public class ColumbidModel<T extends AbstractColumbidEntity> extends GeoModel<T> {

    public ColumbidModel() {
    }

    @Override
    public @NotNull ResourceLocation getModelResource(@NotNull T animatable) {
        return ColumbidDefinition.MODEL;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(@NotNull T animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(@NotNull T animatable) {
        return ColumbidDefinition.ANIMATION;
    }
}