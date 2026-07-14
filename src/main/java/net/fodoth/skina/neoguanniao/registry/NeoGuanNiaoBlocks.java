package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlock;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathVariant;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.feed.BreadcrumbPileBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoGuanNiaoBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NeoGuanNiao.MODID);

    public static final DeferredBlock<Block> BREADCRUMBS;

    public static final DeferredBlock<Block> SMALL_BIRD_CAGE;
    public static final DeferredBlock<Block> MEDIUM_BIRD_CAGE;
    public static final DeferredBlock<Block> LARGE_BIRD_CAGE;

    public static final DeferredBlock<Block> WOODEN_BIRD_BATH;
    public static final DeferredBlock<Block> STONE_BIRD_BATH;
    public static final DeferredBlock<Block> BIRD_BATH;

    public static final DeferredBlock<Block> WOODEN_BIRD_BATH_2;
    public static final DeferredBlock<Block> STONE_BIRD_BATH_2;
    public static final DeferredBlock<Block> BIRD_BATH_2;

    private NeoGuanNiaoBlocks() {
    }

    private static DeferredBlock<Block> registerBirdCage(BirdCageVariant variant) {
        return BLOCKS.register(
                variant.id(),
                () -> new BirdCageBlock(
                        variant,
                        Properties.of()
                                .strength(1.5F, 6.0F)
                                .sound(SoundType.WOOD)
                                .noOcclusion().requiresCorrectToolForDrops()
                                .destroyTime(1.5F)
                )
        );
    }

    private static DeferredBlock<Block> registerBirdBath(BirdBathVariant variant) {
        return BLOCKS.register(
                variant.id(),
                () -> new BirdBathBlock(
                        variant,
                        Properties.of()
                                .strength(1.8F, 6.0F)
                                .sound(variant.soundType())
                                .dynamicShape()
                                .noOcclusion().requiresCorrectToolForDrops()
                                .destroyTime(1.5F)
                )
        );
    }

    static {
        BREADCRUMBS = BLOCKS.register(
                "breadcrumbs",
                () -> new BreadcrumbPileBlock(
                        Properties.of()
                                .instabreak()
                                .replaceable()
                                .sound(SoundType.GRASS)
                                .dynamicShape()
                )
        );

        SMALL_BIRD_CAGE = registerBirdCage(BirdCageVariant.SMALL);
        MEDIUM_BIRD_CAGE = registerBirdCage(BirdCageVariant.MEDIUM);
        LARGE_BIRD_CAGE = registerBirdCage(BirdCageVariant.LARGE);

        WOODEN_BIRD_BATH = registerBirdBath(BirdBathVariant.WOODEN_BIRD_BATH);
        STONE_BIRD_BATH = registerBirdBath(BirdBathVariant.STONE_BIRD_BATH);
        BIRD_BATH = registerBirdBath(BirdBathVariant.BIRD_BATH);

        WOODEN_BIRD_BATH_2 = registerBirdBath(BirdBathVariant.WOODEN_BIRD_BATH_2);
        STONE_BIRD_BATH_2 = registerBirdBath(BirdBathVariant.STONE_BIRD_BATH_2);
        BIRD_BATH_2 = registerBirdBath(BirdBathVariant.BIRD_BATH_2);
    }
}