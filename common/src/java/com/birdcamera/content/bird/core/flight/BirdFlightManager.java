package com.birdcamera.content.bird.core.flight;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class BirdFlightManager {
   private BirdFlightManager() {
   }

   @Deprecated
   @NotNull
   public static Vec3 blendMovement(Vec3 current, Vec3 desired, double desiredWeight) {
      double weight = Mth.clamp(desiredWeight, 0.0, 1.0);
      return current.scale(1.0 - weight).add(desired.scale(weight));
   }

   @Deprecated
   @NotNull
   public static Vec3 steerToward(Mob bird, Vec3 target, double speed, double minVertical, double maxVertical) {
      Vec3 toTarget = target.subtract(bird.position());
      Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);
      if (horizontal.lengthSqr() <= 1.0E-4) {
         horizontal = bird.getDeltaMovement().multiply(1.0, 0.0, 1.0);
      }

      if (horizontal.lengthSqr() <= 1.0E-4) {
         horizontal = bird.getLookAngle().multiply(1.0, 0.0, 1.0);
      }

      if (horizontal.lengthSqr() <= 1.0E-4) {
         horizontal = new Vec3(1.0, 0.0, 0.0);
      }

      double vertical = Mth.clamp(toTarget.y * 0.12, minVertical, maxVertical);
      return horizontal.normalize().scale(speed).add(0.0, vertical, 0.0);
   }

   public static double decelerateNearLanding(double baseSpeed, double distance, double decelerationDistance, double minFactor) {
      if (!(decelerationDistance <= 0.0) && !(distance >= decelerationDistance)) {
         double factor = Mth.clamp(distance / decelerationDistance, minFactor, 1.0);
         return baseSpeed * factor;
      } else {
         return baseSpeed;
      }
   }

   public static boolean isStalledInAir(Mob bird, int timeFlying, double minMovementSqr) {
      return timeFlying > 15 && !bird.onGround() && bird.getDeltaMovement().lengthSqr() < minMovementSqr;
   }

   public static void faceMovement(Mob bird, Vec3 movement, float maxPitchDegrees) {
      double horizontalLength = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
      if (!(horizontalLength <= 1.0E-4)) {
         float yaw = (float)(Mth.atan2(movement.z, movement.x) * (180.0 / Math.PI)) - 90.0F;
         float pitch = Mth.clamp((float)(-(Math.atan2(movement.y, horizontalLength) * (180.0 / Math.PI))), -maxPitchDegrees, maxPitchDegrees);
         bird.setYRot(yaw);
         bird.yRotO = yaw;
         bird.yBodyRot = yaw;
         bird.yHeadRot = yaw;
         bird.yHeadRotO = yaw;
         bird.setXRot(pitch);
         bird.xRotO = pitch;
      }
   }

   public static boolean faceGroundMovement(Mob bird, Vec3 movement, double minHorizontalSpeedSqr) {
      if (movement.lengthSqr() <= minHorizontalSpeedSqr) {
         return false;
      } else {
         float yaw = (float)(Mth.atan2(movement.z, movement.x) * (180.0 / Math.PI)) - 90.0F;
         bird.setYRot(yaw);
         bird.yRotO = yaw;
         bird.yBodyRot = yaw;
         bird.yHeadRot = yaw;
         bird.yHeadRotO = yaw;
         bird.setXRot(0.0F);
         bird.xRotO = 0.0F;
         return true;
      }
   }

   public static boolean shouldPlayFlyAnimation(
      BirdFlightAware bird, boolean airborneState, boolean onGround, boolean noGravity, Vec3 movement, int airborneGraceTicks
   ) {
      if (bird.isBirdFlightActive() || airborneState) {
         return true;
      } else if (onGround) {
         return false;
      } else if (airborneGraceTicks > 0) {
         return true;
      } else if (noGravity || bird.isBirdLanding() || bird.isBirdEscaping()) {
         return true;
      } else {
         return movement.y > -0.85 ? true : movement.lengthSqr() > 0.001;
      }
   }
}
