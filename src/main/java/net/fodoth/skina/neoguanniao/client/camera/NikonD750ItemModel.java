package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.content.camera.NikonD750Item;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NikonD750ItemModel
extends GeoModel<NikonD750Item> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("neoguanniao", "geo/nikon_d750.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("neoguanniao", "textures/item/nikon_d750.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath("neoguanniao", "animations/nikon_d750.animation.json");

    public ResourceLocation getModelResource(NikonD750Item animatable) {
        return MODEL;
    }

    public ResourceLocation getTextureResource(NikonD750Item animatable) {
        return TEXTURE;
    }

    public ResourceLocation getAnimationResource(NikonD750Item animatable) {
        return ANIMATION;
    }
}

