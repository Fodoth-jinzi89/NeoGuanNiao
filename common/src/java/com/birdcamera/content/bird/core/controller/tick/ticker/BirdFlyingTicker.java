package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdFlyingController;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlyingDatum;
import com.birdcamera.content.bird.core.flight.BirdFlightBoids;
import com.birdcamera.content.bird.core.flight.BirdFlightManager;
import com.birdcamera.content.bird.core.flight.BirdFlightTargeting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BirdFlyingTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   // 群聚向量采样间隔（每 4 tick 计算一次，降低性能开销，与原版 2.9.1 一致）
   private static final int FLOCK_SAMPLE_INTERVAL = 4;

   public int hoverRetargetTicks;
   public int flyingTime;
   private Vec3 cachedFlockHeading = Vec3.ZERO;

   public BirdFlyingTicker() {
      super(true, false);
   }

   @Override
   public void tick() {
      if (this.shouldTickCommon() && !this.isFrozen() && this.getTicks() <= 0 && this.bird().isInWater()) {
         this.tickWaterEscape();
      } else {
         super.tick();
      }
   }

   @Override
   protected void onExpire() {
      super.onExpire();
      if (!this.bird().getFlyingController().isLandingFlight && this.bird().getTickController().getTickTimer().getBirdLandingTicker().getTicks() == 0) {
         this.bird().getFlyingController().beginLandingFlight();
         this.flyingTime = 0;
         this.cachedFlockHeading = Vec3.ZERO;
      }
   }

   @Override
   protected void run() {
      this.tickWaterEscape();
      this.tickFlight();
      this.tickAmbientAirCruise();
      this.tickGroundMovementFacing();
   }

   private void tickWaterEscape() {
      if (this.bird().isInWater()) {
         BirdFlyingController<T> flyingController = this.bird().getFlyingController();
         BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
         BirdData birdData = this.bird().getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         RandomSource random = this.bird().getRandom();
         this.bird().getNavigation().stop();
         flyingController.isLandingFlight = false;
         flyingController.isEscapeFlightActive = false;
         flyingController.flightTarget = flyingController.findAirCruiseTarget(false);
         int minDuration = flyingDatum.waterEscapeMinDuration();
         int randomDuration = random.nextInt(flyingDatum.waterEscapeRandomDuration());
         this.setTicks(Math.max(this.getTicks(), minDuration + randomDuration));
         int retargetMin = flyingDatum.waterEscapeHoverRetargetMin();
         int retargetMax = flyingDatum.waterEscapeHoverRetargetMax();
         this.hoverRetargetTicks = Math.clamp((long)this.hoverRetargetTicks, retargetMin, retargetMax);
         this.bird().setNoGravity(true);
         stateController.setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.waterEscapeBehaviorTicks());
         Vec3 toTarget = flyingController.flightTarget.subtract(this.bird().position());
         Vec3 horizontal = toTarget.multiply(1.0, 0.0, 1.0);
         if (horizontal.length() <= 1.0E-4) {
            horizontal = flyingController.randomHorizontalDirection();
         }

         Vec3 direction = horizontal.normalize();
         double horizontalSpeed = flyingDatum.waterEscapeHorizontalSpeed();
         double verticalSpeed = flyingDatum.waterEscapeVerticalSpeed();
         Vec3 movement = direction.scale(horizontalSpeed).add(0.0, verticalSpeed, 0.0);
         this.bird().setDeltaMovement(movement);
         flyingController.faceFlightDirection(movement);
         this.bird().xxa = 0.0F;
         this.bird().hasImpulse = true;
      }
   }

   private void tickFlight() {
      BirdFlyingController<T> flyingController = this.bird().getFlyingController();
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      BirdData birdData = this.bird().getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      this.bird().getNavigation().stop();
      this.bird().setNoGravity(true);
      this.bird().xxa = 0.0F;
      this.flyingTime++;
      BirdBehaviorState flightState = flyingController.isEscapeFlightActive ? BirdBehaviorState.FLEEING : BirdBehaviorState.FLYING;
      stateController.setBehaviorState(flightState);
      if (flyingController.flightTarget == null) {
         if (flyingController.isLandingFlight) {
            flyingController.flightTarget = flyingController.findLandingTarget();
            if (flyingController.flightTarget == null) {
               flyingController.extendCruiseAfterUnsafeLanding();
               return;
            }
         } else {
            flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
         }
      }

      Vec3 toTarget = flyingController.flightTarget.subtract(this.bird().position());
      double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
      if (flyingController.isLandingFlight) {
         double reachDistance = flyingDatum.flightLandingReachDistance();
         boolean canLand = this.bird().onGround() || reachDistance >= toTarget.length();
         if (canLand) {
            flyingController.finishFlight();
            return;
         }
      }

      if (flyingController.isMountFlight && toTarget.length() <= 0.15) {
         this.bird().setPos(flyingController.flightTarget);
         this.bird().setDeltaMovement(Vec3.ZERO);
         flyingController.finishFlight();
      } else {
         double reachDistance = flyingDatum.flightTargetReachDistance();
         if (!flyingController.isMountFlight) {
            if (toTarget.length() >= reachDistance && this.hoverRetargetTicks > 0) {
               this.hoverRetargetTicks--;
            } else {
               flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
               toTarget = flyingController.flightTarget.subtract(this.bird().position());
               horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            }
         }

         Vec3 direction = toTarget.length() > 1.0E-4 ? toTarget.normalize() : flyingController.randomHorizontalDirection();
         Vec3 horizontalDirection = BirdFlightTargeting.normalizeHorizontal(new Vec3(direction.x, 0.0, direction.z), this.bird().getDeltaMovement());
         if (!flyingController.isLandingFlight) {
            double range = flyingDatum.flockRange();
            double separation = flyingDatum.flockSeparation();
            double alignment = flyingDatum.flockAlignment();
            double cohesion = flyingDatum.flockCohesion();
            double weightEscape = flyingDatum.flockWeightEscape();
            double flockWeight = flyingController.isEscapeFlightActive ? flyingDatum.flockEscapeWeight() : flyingDatum.flockAmbientWeight();
            if (this.flyingTime == 1 || Math.floorMod(this.bird().tickCount + this.bird().getId(), FLOCK_SAMPLE_INTERVAL) == 0) {
               this.cachedFlockHeading = BirdFlightBoids.sameTypeHeading(
                  this.bird(), range, separation, alignment, cohesion, weightEscape, 0.0
               );
            }

            Vec3 flockHeading = this.cachedFlockHeading.add(
               BirdFlightTargeting.randomHorizontalDirection(this.bird().getRandom()).scale(flockWeight)
            );
            if (flockHeading.length() > 1.0E-4) {
               horizontalDirection = BirdFlightTargeting.normalizeHorizontal(horizontalDirection.add(flockHeading), horizontalDirection);
            }
         }

         double speed = this.getSpeed(horizontalDistance);
         Vec3 desired = this.getDesired(toTarget, horizontalDirection, speed);
         double movementScale = flyingDatum.flightMovementScale();
         double desiredScale = flyingDatum.flightDesiredScale();
         Vec3 movement = this.bird().getDeltaMovement().scale(movementScale).add(desired.scale(desiredScale));
         if (!flyingController.isLandingFlight) {
            double stalledThreshold = flyingDatum.flightStalledThreshold();
            if (BirdFlightManager.isStalledInAir(this.bird(), this.flyingTime, stalledThreshold)) {
               flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
               double minSpeed = flyingDatum.flightStalledMinSpeed();
               double verticalBoost = flyingDatum.flightStalledVerticalBoost();
               movement = horizontalDirection.scale(Math.max(speed, minSpeed)).add(0.0, verticalBoost, 0.0);
            }
         }

         this.bird().setDeltaMovement(movement);
         flyingController.faceFlightDirection(movement);
         this.bird().hasImpulse = true;
      }
   }

   @NotNull
   private Vec3 getDesired(Vec3 toTarget, Vec3 horizontalDirection, double speed) {
      BirdData birdData = this.bird().getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      BirdFlyingController<T> flyingController = this.bird().getFlyingController();
      double vertical;
      if (flyingController.isLandingFlight) {
         double factor = flyingDatum.flightVerticalLandingFactor();
         double clampMin = flyingDatum.flightVerticalClampMin();
         double clampMax = flyingDatum.flightVerticalClampMax();
         vertical = Mth.clamp(toTarget.y * factor + flyingDatum.flightLandingHoverBob(), clampMin, clampMax);
      } else {
         double factor = flyingDatum.flightVerticalAmbientFactor();
         double min = flyingDatum.flightVerticalAmbientMin();
         double max = flyingDatum.flightVerticalAmbientMax();
         double hoverBob = Math.sin((double)(this.bird().tickCount + this.bird().getId()) * flyingDatum.flightHoverBobFrequency())
            * flyingDatum.flightHoverBobAmplitude();
         vertical = Mth.clamp(toTarget.y * factor + hoverBob, min, max);
      }

      return new Vec3(horizontalDirection.x * speed, vertical, horizontalDirection.z * speed);
   }

   private double getSpeed(double horizontalDistance) {
      BirdData birdData = this.bird().getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      BirdFlyingController<T> flyingController = this.bird().getFlyingController();
      double speed;
      if (flyingController.isEscapeFlightActive) {
         speed = flyingDatum.flightEscapeSpeed();
      } else if (flyingController.isLandingFlight) {
         speed = flyingDatum.flightLandingSpeed();
      } else {
         speed = flyingDatum.flightAmbientSpeed();
      }

      if (flyingController.isLandingFlight) {
         double decalDistance = flyingDatum.flightLandingDecalDistance();
         double decalFactor = flyingDatum.flightLandingDecalFactor();
         speed = BirdFlightManager.decelerateNearLanding(speed, horizontalDistance, decalDistance, decalFactor);
      }

      return speed;
   }

   private void tickAmbientAirCruise() {
      if (this.bird().getFlyingController().canStartAmbientAirCruise()) {
         BirdData birdData = this.bird().getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         int chance = this.bird().isTame() ? flyingDatum.ambientAirCruiseChanceTame() : flyingDatum.ambientAirCruiseChanceWild();
         if (this.bird().getRandom().nextInt(chance) == 0) {
            Vec3 target = this.bird().getFlyingController().findAirCruiseTarget(false);
            this.bird().getFlyingController().startShortFlight(target, false);
         }
      }
   }

   private void tickGroundMovementFacing() {
      if (this.bird().getFlyingController().shouldFaceGroundMovement()) {
         BirdFlightManager.faceGroundMovement(this.bird(), this.bird().getDeltaMovement(), 1.0E-4);
      }
   }
}
