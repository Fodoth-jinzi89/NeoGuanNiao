package com.birdcamera.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BirdCameraItemTags {
   public static final TagKey<Item> BIRD_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "bird_food"));
   public static final TagKey<Item> BIRD_FOOD_FISH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "bird_food_fish"));
   public static final TagKey<Item> SEAGULL_EXTRA_FOOD = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "seagull_extra_food")
   );
   public static final TagKey<Item> BIRD_BREED_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "bird_breed_food"));
   public static final TagKey<Item> BIRD_BREED_FOOD_FISH = TagKey.create(
      Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "bird_breed_food_fish")
   );
   public static final TagKey<Item> FILLED_FOOD_BAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("birdcamera", "filled_food_bag"));

   public static void register() {
   }
}
