package net.fodoth.skina.neoguanniao.client.entity.sparrow;

import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowDefinition;
import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

/**
 * 麻雀模型
 * 使用 GeckoLib 加载麻雀的模型、纹理和动画
 */
public class SparrowModel extends GeoModel<SparrowEntity> {

    public SparrowModel() {
    }

    @Override
    public @NotNull ResourceLocation getModelResource(@NotNull SparrowEntity animatable) {
        return SparrowDefinition.MODEL;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(@NotNull SparrowEntity animatable) {
        return SparrowDefinition.TEXTURE;
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(@NotNull SparrowEntity animatable) {
        return SparrowDefinition.ANIMATION;
    }
}