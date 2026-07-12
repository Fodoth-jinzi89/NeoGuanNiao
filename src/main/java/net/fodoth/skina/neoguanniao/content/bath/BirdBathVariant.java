package net.fodoth.skina.neoguanniao.content.bath;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.shapes.VoxelShape;


public enum BirdBathVariant {

    WOODEN_BIRD_BATH(
            "wooden_bird_bath",
            "geo/wooden_bird_bath.geo.json",
            "textures/block/wooden_bird_bath.png",
            "animations/bird_bath.animation.json",
            SoundType.WOOD
    ),

    STONE_BIRD_BATH(
            "stone_bird_bath",
            "geo/stone_bird_bath.geo.json",
            "textures/block/stone_bird_bath.png",
            "animations/bird_bath.animation.json",
            SoundType.STONE
    ),

    BIRD_BATH(
            "bird_bath",
            "geo/iron_bird_bath.geo.json",
            "textures/block/iron_bird_bath.png",
            "animations/bird_bath.animation.json",
            SoundType.METAL
    ),

    WOODEN_BIRD_BATH_2(
            "wooden_bird_bath_2",
            "geo/wooden_bird_bath_2.geo.json",
            "textures/block/wooden_bird_bath_2.png",
            "animations/bird_bath_2.animation.json",
            SoundType.WOOD
    ),

    STONE_BIRD_BATH_2(
            "stone_bird_bath_2",
            "geo/stone_bird_bath_2.geo.json",
            "textures/block/stone_bird_bath_2.png",
            "animations/bird_bath_2.animation.json",
            SoundType.STONE
    ),

    BIRD_BATH_2(
            "bird_bath_2",
            "geo/iron_bird_bath_2.geo.json",
            "textures/block/iron_bird_bath_2.png",
            "animations/bird_bath_2.animation.json",
            SoundType.METAL
    );




    private final String id;

    private final ResourceLocation model;

    private final ResourceLocation texture;

    private final ResourceLocation animation;

    private final SoundType soundType;

    private final VoxelShape shape;


    BirdBathVariant(
            String id,
            String modelPath,
            String texturePath,
            String animationPath,
            SoundType soundType
    ) {

        this.id = id;

        this.model =
                ResourceLocation.fromNamespaceAndPath(
                        NeoGuanNiao.MODID,
                        modelPath
                );

        this.texture =
                ResourceLocation.fromNamespaceAndPath(
                        NeoGuanNiao.MODID,
                        texturePath
                );

        this.animation =
                ResourceLocation.fromNamespaceAndPath(
                        NeoGuanNiao.MODID,
                        animationPath
                );

        this.soundType = soundType;


        this.shape =
                Block.box(
                        1,
                        0,
                        1,
                        15,
                        24,
                        15
                );
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


    public ResourceLocation animation() {
        return animation;
    }


    public SoundType soundType() {
        return soundType;
    }


    public VoxelShape shape() {
        return shape;
    }
}