package net.fodoth.skina.neoguanniao.client.bath;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdBathModel extends GeoModel<BirdBathBlockEntity> {
    public BirdBathModel() {
    }

    public ResourceLocation getModelResource(BirdBathBlockEntity animatable) {
        return animatable.variant().model();
    }

    public ResourceLocation getTextureResource(BirdBathBlockEntity animatable) {
        return animatable.variant().texture();
    }

    public ResourceLocation getAnimationResource(BirdBathBlockEntity animatable) {
        return animatable.variant().animation();
    }
}
