package com.birdcamera.content.bird.core.model;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record BirdModel(
   ResourceLocation id,
   ResourceLocation location,
   BirdModelRarity rarity,
   boolean natureSpawn,
   boolean breed,
   boolean baby,
   boolean male,
   boolean female,
   boolean unique,
   boolean hidden,
   Set<ResourceLocation> whiteList,
   Set<ResourceLocation> blackList
) {
   public static BirdModel createDefault() {
      return new BirdModel(
         ResourceLocation.fromNamespaceAndPath("birdcamera", "default"),
         ResourceLocation.fromNamespaceAndPath("birdcamera", "default_model"),
         BirdModelRarity.COMMON,
         true,
         true,
         true,
         true,
         true,
         false,
         false,
         Set.of(),
         Set.of()
      );
   }

   public boolean supportsSkin(ResourceLocation skinId) {
      return !this.whiteList.isEmpty() ? this.whiteList.contains(skinId) : !this.blackList.contains(skinId);
   }

   public BirdModel withId(ResourceLocation id) {
      return new BirdModel(
         id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withLocation(ResourceLocation location) {
      return new BirdModel(
         this.id,
         location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withRarity(BirdModelRarity rarity) {
      return new BirdModel(
         this.id,
         this.location,
         rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withNatureSpawn(boolean natureSpawn) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withBreed(boolean breed) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withBaby(boolean baby) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withMale(boolean male) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withFemale(boolean female) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         female,
         this.unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withUnique(boolean unique) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         unique,
         this.hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withHidden(boolean hidden) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         hidden,
         this.whiteList,
         this.blackList
      );
   }

   public BirdModel withWhiteList(Set<ResourceLocation> skins) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         Set.copyOf(skins),
         this.blackList
      );
   }

   public BirdModel withBlackList(Set<ResourceLocation> skins) {
      return new BirdModel(
         this.id,
         this.location,
         this.rarity,
         this.natureSpawn,
         this.breed,
         this.baby,
         this.male,
         this.female,
         this.unique,
         this.hidden,
         this.whiteList,
         Set.copyOf(skins)
      );
   }
}
