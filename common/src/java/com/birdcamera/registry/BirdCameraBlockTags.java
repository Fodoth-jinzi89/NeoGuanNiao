package com.birdcamera.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BirdCameraBlockTags {
   public static final TagKey<Block> BIRD_PERCHES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("birdcamera", "bird_perches"));

   private BirdCameraBlockTags() {
   }
}
