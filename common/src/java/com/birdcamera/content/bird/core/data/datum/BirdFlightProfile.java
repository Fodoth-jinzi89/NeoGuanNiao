package com.birdcamera.content.bird.core.data.datum;

import java.util.Random;

public final class BirdFlightProfile {
   public static final BirdFlightProfile SPARROW = new BirdFlightProfile(2.0, 6.0, 0.24, 0.42, 0.18, 22, 122, 4.0, 18.0, 3.2, 42.0F);
   public static final BirdFlightProfile BUDGERIGAR = new BirdFlightProfile(3.0, 9.0, 0.26, 0.34, 0.2, 90, 260, 4.0, 12.0, 3.8, 42.0F);
   public static final BirdFlightProfile COLUMBID = new BirdFlightProfile(12.0, 24.0, 0.38, 0.44, 0.24, 520, 820, 24.0, 68.0, 9.5, 40.0F);
   public static final BirdFlightProfile NIGHT_HERON = new BirdFlightProfile(7.0, 36.0, 0.4, 0.55, 0.24, 80, 320, 18.0, 64.0, 9.0, 36.0F);
   private final double minCruiseHeight;
   private final double maxCruiseHeight;
   private final double cruiseSpeed;
   private final double escapeSpeed;
   private final double landingSpeed;
   private final int minFlightTicks;
   private final int maxFlightTicks;
   private final double minAirTargetDistance;
   private final double maxAirTargetDistance;
   private final double maxVerticalStep;
   private final float maxPitchDegrees;

   private BirdFlightProfile(
      double minCruiseHeight,
      double maxCruiseHeight,
      double cruiseSpeed,
      double escapeSpeed,
      double landingSpeed,
      int minFlightTicks,
      int maxFlightTicks,
      double minAirTargetDistance,
      double maxAirTargetDistance,
      double maxVerticalStep,
      float maxPitchDegrees
   ) {
      this.minCruiseHeight = minCruiseHeight;
      this.maxCruiseHeight = maxCruiseHeight;
      this.cruiseSpeed = cruiseSpeed;
      this.escapeSpeed = escapeSpeed;
      this.landingSpeed = landingSpeed;
      this.minFlightTicks = minFlightTicks;
      this.maxFlightTicks = Math.max(minFlightTicks, maxFlightTicks);
      this.minAirTargetDistance = minAirTargetDistance;
      this.maxAirTargetDistance = Math.max(minAirTargetDistance, maxAirTargetDistance);
      this.maxVerticalStep = maxVerticalStep;
      this.maxPitchDegrees = maxPitchDegrees;
   }

   public double minCruiseHeight() {
      return this.minCruiseHeight;
   }

   public double maxCruiseHeight() {
      return this.maxCruiseHeight;
   }

   public double cruiseSpeed() {
      return this.cruiseSpeed;
   }

   public double escapeSpeed() {
      return this.escapeSpeed;
   }

   public double landingSpeed() {
      return this.landingSpeed;
   }

   public int minFlightTicks() {
      return this.minFlightTicks;
   }

   public int maxFlightTicks() {
      return this.maxFlightTicks;
   }

   public double minAirTargetDistance() {
      return this.minAirTargetDistance;
   }

   public double maxAirTargetDistance() {
      return this.maxAirTargetDistance;
   }

   public double maxVerticalStep() {
      return this.maxVerticalStep;
   }

   public float maxPitchDegrees() {
      return this.maxPitchDegrees;
   }

   public int randomFlightTicks(Random random) {
      return this.minFlightTicks + random.nextInt(this.maxFlightTicks - this.minFlightTicks + 1);
   }

   public double randomAirTargetDistance(Random random) {
      return this.minAirTargetDistance + random.nextDouble() * (this.maxAirTargetDistance - this.minAirTargetDistance);
   }

   @Override
   public String toString() {
      return "BirdFlightProfile{minCruiseHeight="
         + this.minCruiseHeight
         + ", maxCruiseHeight="
         + this.maxCruiseHeight
         + ", cruiseSpeed="
         + this.cruiseSpeed
         + ", escapeSpeed="
         + this.escapeSpeed
         + ", landingSpeed="
         + this.landingSpeed
         + ", minFlightTicks="
         + this.minFlightTicks
         + ", maxFlightTicks="
         + this.maxFlightTicks
         + ", minAirTargetDistance="
         + this.minAirTargetDistance
         + ", maxAirTargetDistance="
         + this.maxAirTargetDistance
         + ", maxVerticalStep="
         + this.maxVerticalStep
         + ", maxPitchDegrees="
         + this.maxPitchDegrees
         + "}";
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BirdFlightProfile that)
            ? false
            : Double.compare(that.minCruiseHeight, this.minCruiseHeight) == 0
               && Double.compare(that.maxCruiseHeight, this.maxCruiseHeight) == 0
               && Double.compare(that.cruiseSpeed, this.cruiseSpeed) == 0
               && Double.compare(that.escapeSpeed, this.escapeSpeed) == 0
               && Double.compare(that.landingSpeed, this.landingSpeed) == 0
               && this.minFlightTicks == that.minFlightTicks
               && this.maxFlightTicks == that.maxFlightTicks
               && Double.compare(that.minAirTargetDistance, this.minAirTargetDistance) == 0
               && Double.compare(that.maxAirTargetDistance, this.maxAirTargetDistance) == 0
               && Double.compare(that.maxVerticalStep, this.maxVerticalStep) == 0
               && Float.compare(that.maxPitchDegrees, this.maxPitchDegrees) == 0;
      }
   }

   @Override
   public int hashCode() {
      int result = Double.hashCode(this.minCruiseHeight);
      result = 31 * result + Double.hashCode(this.maxCruiseHeight);
      result = 31 * result + Double.hashCode(this.cruiseSpeed);
      result = 31 * result + Double.hashCode(this.escapeSpeed);
      result = 31 * result + Double.hashCode(this.landingSpeed);
      result = 31 * result + this.minFlightTicks;
      result = 31 * result + this.maxFlightTicks;
      result = 31 * result + Double.hashCode(this.minAirTargetDistance);
      result = 31 * result + Double.hashCode(this.maxAirTargetDistance);
      result = 31 * result + Double.hashCode(this.maxVerticalStep);
      return 31 * result + Float.hashCode(this.maxPitchDegrees);
   }
}
