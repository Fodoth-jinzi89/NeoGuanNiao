package net.fodoth.skina.neoguanniao.client.nest;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BirdNestItemModel extends GeoModel<BirdNestItem> {

    @Override
    public ResourceLocation getModelResource(BirdNestItem item) {
        return NeoGuanNiao.resource("geo/bird_nest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BirdNestItem item) {
        return NeoGuanNiao.resource("textures/block/bird_nest.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BirdNestItem item) {
        return NeoGuanNiao.resource("animations/bird_nest.animation.json");
    }
}
