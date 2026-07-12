package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class NeoGuanNiaoBlockTags {

    public static final TagKey<Block> BIRD_PERCHES = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_perches")
    );

    private NeoGuanNiaoBlockTags() {
    }
}