package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

@SuppressWarnings("ConstantConditions")
public final class NeoGuanNiaoBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<BirdCageBlockEntity>> BIRD_CAGE =
            BLOCK_ENTITY_TYPES.register(
                    "bird_cage",
                    () -> BlockEntityType.Builder.of(
                            BirdCageBlockEntity::new,
                            NeoGuanNiaoBlocks.SMALL_BIRD_CAGE.get(),
                            NeoGuanNiaoBlocks.MEDIUM_BIRD_CAGE.get(),
                            NeoGuanNiaoBlocks.LARGE_BIRD_CAGE.get()
                    ).build(null)
            );

    public static final RegistrySupplier<BlockEntityType<BirdBathBlockEntity>> BIRD_BATH =
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

    public static final RegistrySupplier<BlockEntityType<BirdNestBlockEntity>> BIRD_NEST =
            BLOCK_ENTITY_TYPES.register(
                    "bird_nest",
                    () -> BlockEntityType.Builder.of(
                            BirdNestBlockEntity::new,
                            NeoGuanNiaoBlocks.BIRD_NEST.get()
                    ).build(null)
            );

    private NeoGuanNiaoBlockEntityTypes() {}
}
