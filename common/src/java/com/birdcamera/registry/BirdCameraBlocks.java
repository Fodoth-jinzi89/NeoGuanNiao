package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bath.BirdBathBlock;
import com.birdcamera.content.bath.BirdBathVariant;
import com.birdcamera.content.cage.BirdCageBlock;
import com.birdcamera.content.cage.BirdCageVariant;
import com.birdcamera.content.feed.BreadcrumbPileBlock;
import com.birdcamera.content.nest.BirdNestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public class BirdCameraBlocks {

    // 面包屑
    public static final Block BREADCRUMBS = register("breadcrumbs",
            new BreadcrumbPileBlock(BlockBehaviour.Properties.of()
                    .strength(0.1F)
                    .sound(SoundType.GRAVEL)));

    // 小鸟笼
    public static final Block SMALL_BIRD_CAGE = register("small_bird_cage",
            new BirdCageBlock(BirdCageVariant.SMALL, BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 中鸟笼
    public static final Block MEDIUM_BIRD_CAGE = register("medium_bird_cage",
            new BirdCageBlock(BirdCageVariant.MEDIUM, BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 大鸟笼
    public static final Block LARGE_BIRD_CAGE = register("large_bird_cage",
            new BirdCageBlock(BirdCageVariant.LARGE, BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 木质鸟盆
    public static final Block WOODEN_BIRD_BATH = register("wooden_bird_bath",
            new BirdBathBlock(BirdBathVariant.WOODEN_BIRD_BATH, BlockBehaviour.Properties.of()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 石质鸟盆
    public static final Block STONE_BIRD_BATH = register("stone_bird_bath",
            new BirdBathBlock(BirdBathVariant.STONE_BIRD_BATH, BlockBehaviour.Properties.of()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 鸟盆（普通）
    public static final Block BIRD_BATH = register("bird_bath",
            new BirdBathBlock(BirdBathVariant.BIRD_BATH, BlockBehaviour.Properties.of()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 木制鸟盆 II
    public static final Block WOODEN_BIRD_BATH_2 = register("wooden_bird_bath_2",
            new BirdBathBlock(BirdBathVariant.WOODEN_BIRD_BATH_2, BlockBehaviour.Properties.of()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 石质鸟盆 II
    public static final Block STONE_BIRD_BATH_2 = register("stone_bird_bath_2",
            new BirdBathBlock(BirdBathVariant.STONE_BIRD_BATH_2, BlockBehaviour.Properties.of()
                    .strength(1.8F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 铁质鸟盆 II
    public static final Block BIRD_BATH_2 = register("bird_bath_2",
            new BirdBathBlock(BirdBathVariant.BIRD_BATH_2, BlockBehaviour.Properties.of()
                    .strength(2.0F, 7.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // 鸟巢
    public static final Block BIRD_NEST = register("bird_nest",
            new BirdNestBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F)
                    .sound(SoundType.GRASS)
                    .noOcclusion()));

    private static Block register(String id, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, BirdCameraMod.id(id), block);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册方块...");
    }
}
