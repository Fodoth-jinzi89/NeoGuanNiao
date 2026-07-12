package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoGuanNiaoBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoGuanNiao.MODID);

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BirdCageBlockEntity>> BIRD_CAGE =
            BLOCK_ENTITY_TYPES.register(
                    "bird_cage",
                    () -> BlockEntityType.Builder.of(
                            BirdCageBlockEntity::new,
                            NeoGuanNiaoBlocks.SMALL_BIRD_CAGE.get(),
                            NeoGuanNiaoBlocks.MEDIUM_BIRD_CAGE.get(),
                            NeoGuanNiaoBlocks.LARGE_BIRD_CAGE.get()
                    ).build(null)
            );

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BirdBathBlockEntity>> BIRD_BATH =
            BLOCK_ENTITY_TYPES.register(
                    "bird_bath",
                    () -> BlockEntityType.Builder.of(
                            BirdBathBlockEntity::new,
                            NeoGuanNiaoBlocks.WOODEN_BIRD_BATH.get(),
                            NeoGuanNiaoBlocks.STONE_BIRD_BATH.get(),
                            NeoGuanNiaoBlocks.BIRD_BATH.get(),
                            NeoGuanNiaoBlocks.WOODEN_BIRD_BATH_2.get(),
                            NeoGuanNiaoBlocks.STONE_BIRD_BATH_2.get(),
                            NeoGuanNiaoBlocks.BIRD_BATH_2.get()
                    ).build(null)
            );


    private NeoGuanNiaoBlockEntityTypes() {}
}