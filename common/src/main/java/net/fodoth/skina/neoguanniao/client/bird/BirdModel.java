package net.fodoth.skina.neoguanniao.client.bird;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
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
        return animatable.getBirdData().animation().modelAnimationMap().get(animatable.getModelId());
    }
}