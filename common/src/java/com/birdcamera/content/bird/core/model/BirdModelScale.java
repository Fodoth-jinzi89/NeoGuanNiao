package com.birdcamera.content.bird.core.model;

import com.birdcamera.content.bird.core.data.datum.BirdModelScaleProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public final class BirdModelScale {
   public static final String NBT_KEY = "BirdModelScale";
   public static final float DEFAULT_INDIVIDUAL_SCALE = 1.0F;

   private BirdModelScale() {
   }

   public static float randomIndividualScale(RandomSource random, BirdModelScaleProfile profile) {
      return Mth.lerp(random.nextFloat(), profile.minIndividualScale(), profile.maxIndividualScale());
   }

   public static float inheritIndividualScale(RandomSource random, float firstParentScale, float secondParentScale, BirdModelScaleProfile profile) {
      float average = (sanitize(firstParentScale, profile) + sanitize(secondParentScale, profile)) * 0.5F;
      float smallMutation = (random.nextFloat() - 0.5F) * 0.06F;
      return random.nextFloat() < 0.12F ? randomIndividualScale(random, profile) : sanitize(average + smallMutation, profile);
   }

   public static float renderScale(BirdModelScaleProfile profile, float individualScale) {
      return profile.baseRenderScale() * sanitize(individualScale, profile);
   }

   public static float sanitize(float scale, BirdModelScaleProfile profile) {
      return Float.isFinite(scale) && !(scale <= 0.0F) ? Math.clamp(scale, profile.minIndividualScale(), profile.maxIndividualScale()) : 1.0F;
   }

   public static void save(@NotNull CompoundTag compoundTag, float individualScale, BirdModelScaleProfile profile) {
      compoundTag.putFloat("BirdModelScale", sanitize(individualScale, profile));
   }

   public static float load(@NotNull CompoundTag compoundTag, BirdModelScaleProfile profile) {
      return !compoundTag.contains("BirdModelScale", 5) ? 1.0F : sanitize(compoundTag.getFloat("BirdModelScale"), profile);
   }

   public static boolean approximatelyEqual(float a, float b, float epsilon) {
      return Math.abs(a - b) < epsilon;
   }
}
