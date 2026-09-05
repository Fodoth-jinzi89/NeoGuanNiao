package com.birdcamera.content.bird.core.flight;

import java.util.List;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class BirdFlightBoids {
   private BirdFlightBoids() {
   }

   @NotNull
   public static Vec3 sameTypeHeading(
      PathfinderMob bird,
      double radius,
      double separationRadius,
      double cohesionWeight,
      double alignmentWeight,
      double separationWeight,
      double randomnessWeight
   ) {
      List<PathfinderMob> nearby = bird.level().getEntitiesOfClass(PathfinderMob.class, bird.getBoundingBox().inflate(radius), other -> {
         if (other != bird && other.isAlive() && other.getType() == bird.getType() && other instanceof BirdFlightAware aware && aware.isBirdFlightActive()) {
            return true;
         }

         return false;
      });
      return headingFrom(bird, nearby, separationRadius, cohesionWeight, alignmentWeight, separationWeight, randomnessWeight);
   }

   @NotNull
   public static Vec3 headingFrom(
      PathfinderMob bird,
      List<? extends PathfinderMob> nearby,
      double separationRadius,
      double cohesionWeight,
      double alignmentWeight,
      double separationWeight,
      double randomnessWeight
   ) {
      if (nearby.isEmpty()) {
         return randomHeading(bird, randomnessWeight);
      } else {
         Vec3 separation = Vec3.ZERO;
         Vec3 alignment = Vec3.ZERO;
         Vec3 center = Vec3.ZERO;
         int alignmentCount = 0;
         int centerCount = 0;
         double separationSqr = separationRadius * separationRadius;

         for (PathfinderMob other : nearby) {
            Vec3 offset = bird.position().subtract(other.position());
            double distanceSqr = offset.lengthSqr();
            if (distanceSqr > 1.0E-4 && distanceSqr < separationSqr) {
               double distance = Math.sqrt(distanceSqr);
               separation = separation.add(offset.normalize().scale((separationRadius - distance) / separationRadius));
            }

            Vec3 otherMovement = other.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (otherMovement.lengthSqr() > 1.0E-4) {
               alignment = alignment.add(otherMovement.normalize());
               alignmentCount++;
            }

            center = center.add(other.position());
            centerCount++;
         }

         Vec3 heading = Vec3.ZERO;
         if (separation.lengthSqr() > 1.0E-4) {
            heading = heading.add(separation.normalize().scale(separationWeight));
         }

         if (alignmentCount > 0 && alignment.lengthSqr() > 1.0E-4) {
            heading = heading.add(alignment.normalize().scale(alignmentWeight));
         }

         if (centerCount > 0) {
            Vec3 cohesion = center.scale(1.0 / (double)centerCount).subtract(bird.position()).multiply(1.0, 0.0, 1.0);
            if (cohesion.lengthSqr() > 1.0E-4) {
               heading = heading.add(cohesion.normalize().scale(cohesionWeight));
            }
         }

         return heading.add(randomHeading(bird, randomnessWeight));
      }
   }

   @NotNull
   private static Vec3 randomHeading(PathfinderMob bird, double randomnessWeight) {
      return randomnessWeight <= 0.0 ? Vec3.ZERO : BirdFlightTargeting.randomHorizontalDirection(bird.getRandom()).scale(randomnessWeight);
   }
}
