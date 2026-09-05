package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

import software.bernie.geckolib.renderer.GeoBlockRenderer;


public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity> {


    public BirdCageRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(new BirdCageModel());
    }

    public AABB getRenderBoundingBox(BirdCageBlockEntity birdCage) {
        var pos = birdCage.getBlockPos();
        double height = switch (birdCage.variant()) {
            case SMALL -> 1.0D;
            case MEDIUM -> 2.0D;
            case LARGE -> 3.0D;
        };
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + height, pos.getZ() + 1.0D
        );
    }
}



