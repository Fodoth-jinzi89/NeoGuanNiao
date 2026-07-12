package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdCageItemModel extends GeoModel<BirdCageItem> {
    public BirdCageItemModel() {
    }

    public ResourceLocation getModelResource(BirdCageItem animatable) {
        return animatable.variant().model();
    }

    public ResourceLocation getTextureResource(BirdCageItem animatable) {
        return animatable.variant().texture();
    }

    public ResourceLocation getAnimationResource(BirdCageItem animatable) {
        return BirdCageVariant.ANIMATION;
    }
}
