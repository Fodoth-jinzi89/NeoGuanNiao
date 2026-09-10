package net.fodoth.skina.neoguanniao.client.bath;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdBathItemModel extends GeoModel<BirdBathItem> {

    @Override
    public ResourceLocation getModelResource(BirdBathItem animatable) {
        return animatable.variant().model();
    }


    @Override
    public ResourceLocation getTextureResource(BirdBathItem animatable) {
        return animatable.variant().texture();
    }


    @Override
    public ResourceLocation getAnimationResource(BirdBathItem animatable) {
        return animatable.variant().animation();
    }
}