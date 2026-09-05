package com.birdcamera.content.cage;

import com.birdcamera.BirdCameraMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum BirdCageVariant implements StringRepresentable {
    SMALL("small",
            BirdCameraMod.id("geo/small_bird_cage.geo.json"),
            BirdCameraMod.id("textures/block/small_bird_cage.png"),
            makeShape(3.0, 0.0, 3.0, 13.0, 2.0, 13.0,
                    4.0, 2.0, 4.0, 12.0, 12.0, 12.0,
                    5.0, 12.0, 5.0, 11.0, 14.0, 11.0,
                    7.0, 14.0, 7.0, 9.0, 16.0, 9.0),
            0.5f),
    MEDIUM("medium",
            BirdCameraMod.id("geo/medium_bird_cage.geo.json"),
            BirdCameraMod.id("textures/block/medium_bird_cage.png"),
            makeShape(2.0, 0.0, 2.0, 14.0, 2.0, 14.0,
                    3.0, 2.0, 3.0, 13.0, 14.0, 13.0,
                    4.0, 14.0, 4.0, 12.0, 16.0, 12.0,
                    7.0, 16.0, 7.0, 9.0, 18.0, 9.0),
            0.75f),
    LARGE("large",
            BirdCameraMod.id("geo/large_bird_cage.geo.json"),
            BirdCameraMod.id("textures/block/large_bird_cage.png"),
            makeShape(1.0, 0.0, 1.0, 15.0, 2.0, 15.0,
                    2.0, 2.0, 2.0, 14.0, 16.0, 14.0,
                    3.0, 16.0, 3.0, 13.0, 18.0, 13.0,
                    7.0, 18.0, 7.0, 9.0, 20.0, 9.0),
            1.0f);

    private static VoxelShape makeShape(
            double x1, double y1, double z1, double x2, double y2, double z2,
            double x3, double y3, double z3, double x4, double y4, double z4,
            double x5, double y5, double z5, double x6, double y6, double z6,
            double x7, double y7, double z7, double x8, double y8, double z8) {
        return Shapes.or(
                Block.box(x1, y1, z1, x2, y2, z2),
                Block.box(x3, y3, z3, x4, y4, z4),
                Block.box(x5, y5, z5, x6, y6, z6),
                Block.box(x7, y7, z7, x8, y8, z8)
        );
    }

    private final String name;
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final VoxelShape shape;
    private final float sizeScale;

    BirdCageVariant(String name, ResourceLocation model, ResourceLocation texture, VoxelShape shape, float sizeScale) {
        this.name = name;
        this.model = model;
        this.texture = texture;
        this.shape = shape;
        this.sizeScale = sizeScale;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public VoxelShape getShape() {
        return shape;
    }

    public float getSizeScale() {
        return sizeScale;
    }

    public static BirdCageVariant byName(String name) {
        for (BirdCageVariant v : values()) {
            if (v.getSerializedName().equals(name)) {
                return v;
            }
        }
        return SMALL;
    }
}
