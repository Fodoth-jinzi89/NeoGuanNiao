package net.fodoth.skina.neoguanniao.client.bird;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

public class BirdModel<T extends AbstractBirdEntity<?>> extends GeoModel<T> {
    @Override public @NotNull ResourceLocation getModelResource(@NotNull T bird) { return bird.getModelResource(); }
    @Override public @NotNull ResourceLocation getTextureResource(@NotNull T bird) { return bird.getTextureResource(); }
    @Override public @NotNull ResourceLocation getAnimationResource(@NotNull T bird) { return bird.getBirdData().animation().modelAnimationMap().get(bird.getModelId()); }
}
