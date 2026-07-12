package net.fodoth.skina.neoguanniao.content.cage;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;


public enum BirdCageVariant {

    SMALL(
            "small_bird_cage",
            "geo/small_bird_cage.geo.json",
            "textures/block/small_bird_cage.png",
            Block.box(1, 0, 1, 15, 16, 15)
    ),

    MEDIUM(
            "medium_bird_cage",
            "geo/medium_bird_cage.geo.json",
            "textures/block/medium_bird_cage.png",
            Block.box(0, 0, 0, 16, 32, 16)
    ),

    LARGE(
            "large_bird_cage",
            "geo/large_bird_cage.geo.json",
            "textures/block/large_bird_cage.png",
            Block.box(0, 0, 0, 16, 48, 16)
    );


    public static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    NeoGuanNiao.MODID,
                    "animations/bird_cage.animation.json"
            );


    private final String id;
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final VoxelShape shape;


    BirdCageVariant(
            String id,
            String modelPath,
            String texturePath,
            VoxelShape shape
    ) {
        this.id = id;

        this.model = ResourceLocation.fromNamespaceAndPath(
                NeoGuanNiao.MODID,
                modelPath
        );

        this.texture = ResourceLocation.fromNamespaceAndPath(
                NeoGuanNiao.MODID,
                texturePath
        );

        this.shape = shape;
    }


    public String id() {
        return id;
    }


    public ResourceLocation model() {
        return model;
    }


    public ResourceLocation texture() {
        return texture;
    }


    public VoxelShape shape() {
        return shape;
    }
}