package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import software.bernie.geckolib.renderer.GeoBlockRenderer;


public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity> {


    public BirdCageRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(new BirdCageModel());
    }


}
