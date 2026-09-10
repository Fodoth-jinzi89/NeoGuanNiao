package net.fodoth.skina.neoguanniao.client.nest;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdNestModel extends GeoModel<BirdNestBlockEntity> {

    @Override
    public ResourceLocation getModelResource(BirdNestBlockEntity animatable) {
        return NeoGuanNiao.resource("geo/bird_nest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BirdNestBlockEntity animatable) {
        return NeoGuanNiao.resource("textures/block/bird_nest.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BirdNestBlockEntity animatable) {
        return NeoGuanNiao.resource("animations/bird_nest.animation.json");
    }
}
