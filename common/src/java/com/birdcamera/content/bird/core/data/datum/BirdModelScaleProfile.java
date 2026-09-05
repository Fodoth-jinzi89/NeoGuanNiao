package com.birdcamera.content.bird.core.data.datum;

import com.birdcamera.content.bird.core.model.BirdModelScale;
import net.minecraft.util.RandomSource;

public final class BirdModelScaleProfile {
   public static final BirdModelScaleProfile NIGHT_HERON = new BirdModelScaleProfile(1.0F, 0.9F, 1.1F);
   public static final BirdModelScaleProfile SPARROW = new BirdModelScaleProfile(1.0F, 0.86F, 1.14F);
   public static final BirdModelScaleProfile BUDGERIGAR = new BirdModelScaleProfile(0.6F, 0.88F, 1.16F);
   public static final BirdModelScaleProfile COLUMBID = new BirdModelScaleProfile(1.0F, 0.9F, 1.1F);
   private final float baseRenderScale;
   private final float minIndividualScale;
   private final float maxIndividualScale;

   private BirdModelScaleProfile(float baseRenderScale, float minIndividualScale, float maxIndividualScale) {
      this.baseRenderScale = baseRenderScale;
      this.minIndividualScale = minIndividualScale;
      this.maxIndividualScale = maxIndividualScale;
      if (baseRenderScale <= 0.0F) {
         throw new IllegalArgumentException("baseRenderScale must be positive: " + baseRenderScale);
      } else if (minIndividualScale <= 0.0F || maxIndividualScale <= 0.0F) {
         throw new IllegalArgumentException("Individual scale bounds must be positive: min=" + minIndividualScale + ", max=" + maxIndividualScale);
      } else if (minIndividualScale > maxIndividualScale) {
         throw new IllegalArgumentException("minIndividualScale must be <= maxIndividualScale: " + minIndividualScale + " > " + maxIndividualScale);
      }
   }

   public float baseRenderScale() {
      return this.baseRenderScale;
   }

   public float minIndividualScale() {
      return this.minIndividualScale;
   }

   public float maxIndividualScale() {
      return this.maxIndividualScale;
   }

   public float randomIndividualScale(RandomSource random) {
      return BirdModelScale.randomIndividualScale(random, this);
   }

   public float individualScaleRange() {
      return this.maxIndividualScale - this.minIndividualScale;
   }

   public boolean isScaleValid(float scale) {
      return scale >= this.minIndividualScale && scale <= this.maxIndividualScale;
   }

   public float midIndividualScale() {
      return (this.minIndividualScale + this.maxIndividualScale) * 0.5F;
   }

   @Override
   public String toString() {
      return "BirdModelScaleProfile{baseRenderScale="
         + this.baseRenderScale
         + ", minIndividualScale="
         + this.minIndividualScale
         + ", maxIndividualScale="
         + this.maxIndividualScale
         + "}";
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BirdModelScaleProfile that)
            ? false
            : Float.compare(that.baseRenderScale, this.baseRenderScale) == 0
               && Float.compare(that.minIndividualScale, this.minIndividualScale) == 0
               && Float.compare(that.maxIndividualScale, this.maxIndividualScale) == 0;
      }
   }

   @Override
   public int hashCode() {
      int result = Float.hashCode(this.baseRenderScale);
      result = 31 * result + Float.hashCode(this.minIndividualScale);
      return 31 * result + Float.hashCode(this.maxIndividualScale);
   }

   public static BirdModelScaleProfile of(float baseRenderScale, float minIndividualScale, float maxIndividualScale) {
      return new BirdModelScaleProfile(baseRenderScale, minIndividualScale, maxIndividualScale);
   }
}
