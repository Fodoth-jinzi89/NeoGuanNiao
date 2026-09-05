package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bath.BirdBathBlockEntity;
import com.birdcamera.content.cage.BirdCageBlockEntity;
import com.birdcamera.content.nest.BirdNestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public class BirdCameraBlockEntityTypes {

    // 鸟笼方块实体
    public static final BlockEntityType<BirdCageBlockEntity> BIRD_CAGE = register("bird_cage",
            BlockEntityType.Builder.of(BirdCageBlockEntity::new,
                    BirdCameraBlocks.SMALL_BIRD_CAGE,
                    BirdCameraBlocks.MEDIUM_BIRD_CAGE,
                    BirdCameraBlocks.LARGE_BIRD_CAGE).build());

    // 鸟盆方块实体
    public static final BlockEntityType<BirdBathBlockEntity> BIRD_BATH = register("bird_bath",
            BlockEntityType.Builder.of(BirdBathBlockEntity::new,
                    BirdCameraBlocks.WOODEN_BIRD_BATH,
                    BirdCameraBlocks.STONE_BIRD_BATH,
                    BirdCameraBlocks.BIRD_BATH,
                    BirdCameraBlocks.WOODEN_BIRD_BATH_2,
                    BirdCameraBlocks.STONE_BIRD_BATH_2,
                    BirdCameraBlocks.BIRD_BATH_2).build());

    // 鸟巢方块实体
    public static final BlockEntityType<BirdNestBlockEntity> BIRD_NEST = register("bird_nest",
            BlockEntityType.Builder.of(BirdNestBlockEntity::new,
                    BirdCameraBlocks.BIRD_NEST).build());

    private static <T extends BlockEntity> BlockEntityType<T> register(String id, BlockEntityType<T> type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BirdCameraMod.id(id), type);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册方块实体类型...");
    }
}
