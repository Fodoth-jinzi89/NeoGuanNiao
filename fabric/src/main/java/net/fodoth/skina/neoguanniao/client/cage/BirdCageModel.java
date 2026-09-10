package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdCageModel extends GeoModel<BirdCageBlockEntity> {
    public BirdCageModel() {
    }

    public ResourceLocation getModelResource(BirdCageBlockEntity animatable) {
        return animatable.variant().model();
    }

    public ResourceLocation getTextureResource(BirdCageBlockEntity animatable) {
        return animatable.variant().texture();
    }

    public ResourceLocation getAnimationResource(BirdCageBlockEntity animatable) {
        return BirdCageVariant.ANIMATION;
    }
}