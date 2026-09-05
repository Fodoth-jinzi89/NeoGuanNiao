package com.birdcamera.content.bird.core.data.datum;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.RawAnimation;

public record BirdAnimationDatum(
   Map<ResourceLocation, ResourceLocation> modelAnimationMap,
   Map<String, RawAnimation> animationMap,
   int preenDuration,
   int preenDurationVariance,
   int idleDuration,
   int idleDurationVariance,
   int otherDuration,
   int otherDurationVariance,
   int trustTickerMaxLimit,
   int trustTickerLimit,
   int maxCuriousAndTrustingIndex,
   int minCuriousAndTrustingIndex,
   float mainIdleAnimationChance
) {
   public BirdAnimationDatum(
      Map<ResourceLocation, ResourceLocation> modelAnimationMap,
      Map<String, RawAnimation> animationMap,
      int preenDuration,
      int preenDurationVariance,
      int idleDuration,
      int idleDurationVariance,
      int otherDuration,
      int otherDurationVariance,
      int trustTickerMaxLimit,
      int trustTickerLimit,
      int maxCuriousAndTrustingIndex,
      int minCuriousAndTrustingIndex,
      float mainIdleAnimationChance
   ) {
      animationMap = Map.copyOf(animationMap);
      this.modelAnimationMap = modelAnimationMap;
      this.animationMap = animationMap;
      this.preenDuration = preenDuration;
      this.preenDurationVariance = preenDurationVariance;
      this.idleDuration = idleDuration;
      this.idleDurationVariance = idleDurationVariance;
      this.otherDuration = otherDuration;
      this.otherDurationVariance = otherDurationVariance;
      this.trustTickerMaxLimit = trustTickerMaxLimit;
      this.trustTickerLimit = trustTickerLimit;
      this.maxCuriousAndTrustingIndex = maxCuriousAndTrustingIndex;
      this.minCuriousAndTrustingIndex = minCuriousAndTrustingIndex;
      this.mainIdleAnimationChance = mainIdleAnimationChance;
   }

   public static BirdAnimationDatum createDefault() {
      return new BirdAnimationDatum(null, Map.of(), 45, 45, 55, 70, 35, 35, 800, 400, 9, 5, 0.9F);
   }

   public static BirdAnimationDatum withAnimationIdAndMap(Map<ResourceLocation, ResourceLocation> animationId, Map<String, RawAnimation> animationMap) {
      return new BirdAnimationDatum(animationId, animationMap, 45, 45, 55, 70, 35, 35, 800, 400, 9, 5, 0.9F);
   }

   public BirdAnimationDatum withCuriousAndTrustingIndexRange(int maxCuriousAndTrustingIndex, int minCuriousAndTrustingIndex) {
      return new BirdAnimationDatum(
         this.modelAnimationMap,
         this.animationMap,
         this.preenDuration,
         this.preenDurationVariance,
         this.idleDuration,
         this.idleDurationVariance,
         this.otherDuration,
         this.otherDurationVariance,
         this.trustTickerMaxLimit,
         this.trustTickerLimit,
         maxCuriousAndTrustingIndex,
         minCuriousAndTrustingIndex,
         this.mainIdleAnimationChance
      );
   }

   public BirdAnimationDatum withMainIdleAnimationChance(int mainIdleAnimationChance) {
      return new BirdAnimationDatum(
         this.modelAnimationMap,
         this.animationMap,
         this.preenDuration,
         this.preenDurationVariance,
         this.idleDuration,
         this.idleDurationVariance,
         this.otherDuration,
         this.otherDurationVariance,
         this.trustTickerMaxLimit,
         this.trustTickerLimit,
         this.maxCuriousAndTrustingIndex,
         this.minCuriousAndTrustingIndex,
         (float)mainIdleAnimationChance
      );
   }
}
