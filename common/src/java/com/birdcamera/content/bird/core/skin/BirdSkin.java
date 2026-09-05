package com.birdcamera.content.bird.core.skin;

import net.minecraft.resources.ResourceLocation;

public record BirdSkin(
   ResourceLocation id,
   ResourceLocation location,
   BirdSkinRarity rarity,
   boolean natureSpawn,
   boolean breed,
   boolean mature,
   boolean baby,
   boolean male,
   boolean female,
   boolean unique,
   boolean hidden
) {
   public static BirdSkin createDefault() {
      return new BirdSkin(
         ResourceLocation.fromNamespaceAndPath("birdcamera", "default"),
         ResourceLocation.fromNamespaceAndPath("birdcamera", "default_skin"),
         BirdSkinRarity.COMMON,
         true,
         true,
         true,
         true,
         true,
         true,
         false,
         false
      );
   }

   public BirdSkin withId(ResourceLocation id) {
      return new BirdSkin(
         id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withLocation(ResourceLocation location) {
      return new BirdSkin(
         this.id, location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withRarity(BirdSkinRarity rarity) {
      return new BirdSkin(
         this.id, this.location, rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withNatureSpawn(boolean natureSpawn) {
      return new BirdSkin(
         this.id, this.location, this.rarity, natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withBreed(boolean breed) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, breed, this.mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withMature(boolean mature) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, mature, this.baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withBaby(boolean baby) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, baby, this.male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withMale(boolean male) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, male, this.female, this.unique, this.hidden
      );
   }

   public BirdSkin withFemale(boolean female) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, female, this.unique, this.hidden
      );
   }

   public BirdSkin withUnique(boolean unique) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, unique, this.hidden
      );
   }

   public BirdSkin withHidden(boolean hidden) {
      return new BirdSkin(
         this.id, this.location, this.rarity, this.natureSpawn, this.breed, this.mature, this.baby, this.male, this.female, this.unique, hidden
      );
   }
}
