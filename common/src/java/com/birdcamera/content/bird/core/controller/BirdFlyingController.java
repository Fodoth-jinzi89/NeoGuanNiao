package com.birdcamera.content.bird.core.controller;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFlyingTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdLandingTicker;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlyingDatum;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.flight.BirdFlightManager;
import com.birdcamera.content.bird.core.flight.BirdFlightTargeting;
import com.birdcamera.registry.BirdCameraBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BirdFlyingController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public boolean isEscapeFlightActive = false;
   public boolean isLandingFlight = false;
   public boolean isMountFlight;
   public Vec3 flightTarget = null;
   private boolean isLandingAdjusted = false;

   public BirdFlyingController() {
      this.isMountFlight = false;
   }

   @Override
   protected void onAttach() {
      super.onAttach();
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      this.bird().setMoveControl(new FlyingMoveControl(this.bird(), miscDatum.maxTurns(), true));
   }

   public boolean isBirdFlightActive() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      boolean hasFlightDuration = timer.getBirdFlyingTicker().getTicks() > 0;
      boolean isLandingFlight = this.bird.isBirdLanding();
      boolean isAirborne = this.bird.getBehaviorStateController().getBehaviorState().isAirborne();
      boolean isInWaterAndNotOnGround = this.bird.isInWater() && !this.bird.onGround();
      return hasFlightDuration || isLandingFlight || isAirborne || isInWaterAndNotOnGround;
   }

   public boolean isBirdFlyingOrLanding() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
      return timer.getBirdFlyingTicker().isRunning()
         || timer.getBirdLandingTicker().isRunning()
         || state == BirdBehaviorState.FLYING
         || state == BirdBehaviorState.FLEEING;
   }

   public boolean isFlightInProgress() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      return timer.getBirdFlyingTicker().isRunning();
   }

   public void startShortFlight(Vec3 target, boolean fleeing) {
      if (!this.bird().isLeashed()) {
         BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
         BirdFlyingTicker<? extends AbstractBirdEntity<?>> flyingTicker = (BirdFlyingTicker<? extends AbstractBirdEntity<?>>)timer.getBirdFlyingTicker();
         BirdBehaviorStateController<T> stateController = this.bird.getBehaviorStateController();
         BirdData birdData = this.bird.getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         if (flyingTicker.getTicks() <= 0 && flyingTicker.flyingTime <= 0 && !this.isLandingFlight) {
            this.isEscapeFlightActive = fleeing;
            this.flightTarget = target == null ? this.findAirCruiseTarget(fleeing) : target;
            flyingTicker.setTicks(
               fleeing
                  ? flyingDatum.escapeAirCruiseMinTicks() + this.bird.getRandom().nextInt(flyingDatum.escapeAirCruiseRandomTicks())
                  : flyingDatum.ambientAirCruiseMinTicks() + this.bird.getRandom().nextInt(flyingDatum.ambientAirCruiseRandomTicks())
            );
            flyingTicker.flyingTime = 0;
            flyingTicker.hoverRetargetTicks = this.nextHoverRetargetDelay();
            this.bird.setNoGravity(true);
            this.bird.getNavigation().stop();
            int stateTicks = fleeing ? flyingDatum.shortFleeTicks() : flyingDatum.shortFlyTicks();
            BirdBehaviorState state = fleeing ? BirdBehaviorState.FLEEING : BirdBehaviorState.FLYING;
            stateController.setBehaviorStateFor(state, stateTicks);
         }
      }
   }

   private int nextHoverRetargetDelay() {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      return flyingDatum.hoverRetargetMinDelay() + this.bird.getRandom().nextInt(flyingDatum.hoverRetargetDelayVariance());
   }

   public void startFlybyFlight(Vec3 target) {
      if (!this.bird().isLeashed()) {
         BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
         BirdFlyingTicker<? extends AbstractBirdEntity<?>> flyingTicker = (BirdFlyingTicker<? extends AbstractBirdEntity<?>>)timer.getBirdFlyingTicker();
         BirdData birdData = this.bird.getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         BirdMiscDatum miscDatum = birdData.misc();
         this.isEscapeFlightActive = false;
         this.isLandingFlight = false;
         this.flightTarget = target == null ? this.findAirCruiseTarget(false) : this.clampFlightTarget(target);
         flyingTicker.setTicks(flyingDatum.minFlybyDuration() + this.bird.getRandom().nextInt(flyingDatum.flybyDurationVariance() + 1));
         flyingTicker.flyingTime = 0;
         flyingTicker.hoverRetargetTicks = flyingDatum.minHoverRetargetTicks() + this.bird.getRandom().nextInt(flyingDatum.hoverRetargetTicksVariance() + 1);
         flyingTicker.setTicks(Math.max(flyingTicker.getTicks(), flyingDatum.minimumFlightTicks()));
         this.bird.getNavigation().stop();
         this.bird.setNoGravity(true);
         this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.minimumFlightTicks());
         Vec3 direction = this.bird.getFlyingController().flightTarget.subtract(this.bird.position()).multiply(1.0, 0.0, 1.0);
         if (direction.length() <= 1.0E-4) {
            direction = this.randomHorizontalDirection();
         }

         Vec3 movement = direction.normalize().scale(flyingDatum.flybyHorizontalSpeed()).add(0.0, flyingDatum.flybyUpwardSpeed(), 0.0);
         this.bird.setDeltaMovement(movement);
         this.faceFlightDirection(movement);
         this.bird.xxa = 0.0F;
         this.bird.hasImpulse = true;
      }
   }

   public boolean startBirdBathMountFlight(Vec3 standPosition) {
      if (standPosition != null && !this.isFlightInProgress() && !this.bird().isLeashed()) {
         BirdData birdData = this.bird.getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         BirdFlyingTicker<? extends AbstractBirdEntity<?>> ticker = (BirdFlyingTicker<? extends AbstractBirdEntity<?>>)this.bird
            .getTickController()
            .getTickTimer()
            .getBirdFlyingTicker();
         this.flightTarget = standPosition;
         this.isMountFlight = true;
         this.isLandingFlight = false;
         this.isEscapeFlightActive = false;
         ticker.setTicks(flyingDatum.birdBathMountFlightTicks());
         ticker.flyingTime = 0;
         ticker.hoverRetargetTicks = 0;
         this.bird.getNavigation().stop();
         this.bird.setNoGravity(true);
         this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.birdBathMountFlightTicks());
         Vec3 direction = standPosition.subtract(this.bird.position());
         Vec3 movement = direction.normalize()
            .scale(Math.min(flyingDatum.birdBathMountHorizontalSpeed(), direction.length() / (double)flyingDatum.birdBathMountFlightTicks()))
            .add(0.0, flyingDatum.birdBathMountUpwardSpeed(), 0.0);
         this.bird.setDeltaMovement(movement);
         this.faceFlightDirection(movement);
         this.bird.xxa = 0.0F;
         this.bird.hasImpulse = true;
         return true;
      } else {
         return false;
      }
   }

   public void finishFlight() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      BirdFlyingTicker<? extends AbstractBirdEntity<?>> flyingTicker = (BirdFlyingTicker<? extends AbstractBirdEntity<?>>)timer.getBirdFlyingTicker();
      BirdData birdData = this.bird.getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      boolean wasEscaping = this.isEscapeFlightActive;
      flyingTicker.setTicks(0);
      flyingTicker.flyingTime = 0;
      this.flightTarget = null;
      flyingTicker.hoverRetargetTicks = 0;
      this.isEscapeFlightActive = false;
      this.isLandingFlight = false;
      this.bird.setNoGravity(false);
      this.bird.noCulling = false;
      int cooldownTicks = wasEscaping
         ? miscDatum.escapeCooldownMin() + this.bird.getRandom().nextInt(miscDatum.escapeCooldownVariance())
         : (
            this.bird.isTame()
               ? miscDatum.tameCooldownMin() + this.bird.getRandom().nextInt(miscDatum.tameCooldownVariance())
               : miscDatum.wildCooldownMin() + this.bird.getRandom().nextInt(miscDatum.wildCooldownVariance())
         );
      this.bird.getTickController().getTickTimer().getBirdLandingTicker().setTicks(cooldownTicks);
      this.bird.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, cooldownTicks, 0, false, false));
      this.bird.setNoActionTime((int)((double)cooldownTicks * 1.2));
      if (flyingTicker.enableLifecycleLog()) {
         BirdCameraMod.LOGGER.info("[Controller] Finish flight with cooldown ticks: {}", cooldownTicks);
      }

      if (this.bird.getBehaviorStateController().getBehaviorState().isAirborne()) {
         this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, miscDatum.postFlightAlertTicks());
      }
   }

   public void beginLandingFlight() {
      Vec3 landingTarget = this.findLandingTarget();
      if (landingTarget == null) {
         this.extendCruiseAfterUnsafeLanding();
      } else {
         BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
         BirdData birdData = this.bird.getBirdData();
         BirdFlyingDatum flyingDatum = birdData.flying();
         this.isLandingFlight = true;
         this.isEscapeFlightActive = false;
         timer.getBirdFlyingTicker()
            .setTicks(flyingDatum.landingFlightMinDuration() + this.bird.getRandom().nextInt(flyingDatum.landingFlightDurationVariance()));
         this.flightTarget = landingTarget;
         timer.getBirdFlyingTicker().hoverRetargetTicks = 0;
         this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.landingFlightStateTicks());
      }
   }

   public void extendCruiseAfterUnsafeLanding() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      this.isLandingFlight = false;
      this.isEscapeFlightActive = false;
      timer.getBirdFlyingTicker()
         .setTicks(flyingDatum.unsafeLandingCruiseMinDuration() + this.bird.getRandom().nextInt(flyingDatum.unsafeLandingCruiseDurationVariance()));
      this.retargetAirCruise(false);
      this.bird.setNoGravity(true);
      this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.unsafeLandingCruiseStateTicks());
   }

   public void retargetAirCruise(boolean fleeing) {
      this.flightTarget = this.findAirCruiseTarget(fleeing);
      this.bird.getTickController().getTickTimer().getBirdFlyingTicker().hoverRetargetTicks = this.nextHoverRetargetDelay();
   }

   public Vec3 findAirCruiseTarget(boolean fleeing) {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      Vec3 direction;
      if (fleeing && this.bird.getFrightController().getFrightSource() != null) {
         Vec3 away = this.bird.position().subtract(this.bird.getFrightController().getFrightSource());
         direction = away.lengthSqr() > 0.01 ? new Vec3(away.x, 0.0, away.z).normalize() : this.randomHorizontalDirection();
      } else {
         direction = this.bird.getRandom().nextInt(flyingDatum.cruiseLookChanceDenominator()) == 0
            ? this.bird.getLookAngle()
            : this.randomHorizontalDirection();
      }

      Vec3 target = BirdFlightTargeting.findAirTarget(this.bird, birdData.flying().flightProfile(), direction, fleeing);
      return target != null
         ? this.clampFlightTarget(target)
         : this.clampFlightTarget(
            this.bird.position().add(0.0, this.bird.onGround() ? flyingDatum.cruiseFallbackHeightGround() : flyingDatum.cruiseFallbackHeightAir(), 0.0)
         );
   }

   public Vec3 findLandingTarget() {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      Vec3 sharedLanding = BirdFlightTargeting.findNearestDryLandingTarget(
         this.bird, flyingDatum.landingSharedRadius(), flyingDatum.landingSharedVerticalRange()
      );
      if (sharedLanding != null) {
         return this.clampFlightTarget(sharedLanding);
      } else {
         BlockPos origin = this.bird.blockPosition();
         BlockPos landing = this.findDryLandingSurface(origin, flyingDatum.landingSurfaceSearchRadius());
         if (landing != null) {
            return this.clampFlightTarget(Vec3.atBottomCenterOf(landing));
         } else {
            for (int attempt = 0; attempt < flyingDatum.landingRandomAttempts(); attempt++) {
               int halfRange = flyingDatum.landingRandomHorizontalRange() / 2;
               int x = origin.getX() + this.bird.getRandom().nextInt(flyingDatum.landingRandomHorizontalRange()) - halfRange;
               int z = origin.getZ() + this.bird.getRandom().nextInt(flyingDatum.landingRandomHorizontalRange()) - halfRange;
               BlockPos candidate = this.findDryLandingSurface(new BlockPos(x, origin.getY(), z), flyingDatum.landingSurfaceSearchRadius());
               if (candidate != null) {
                  return this.clampFlightTarget(Vec3.atBottomCenterOf(candidate));
               }
            }

            return null;
         }
      }
   }

   private BlockPos findDryLandingSurface(BlockPos center, int verticalRange) {
      MutableBlockPos mutable = new MutableBlockPos();

      for (int yOffset = verticalRange; yOffset >= -verticalRange; yOffset--) {
         mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
         if (this.isSafeDryLanding(mutable)) {
            return mutable.immutable();
         }
      }

      return null;
   }

   private boolean isSafeDryLanding(BlockPos pos) {
      return BirdFlightTargeting.isSafeDryLanding(this.bird, pos);
   }

   private Vec3 clampFlightTarget(Vec3 target) {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      double y = Mth.clamp(
         target.y,
         (double)this.bird.level().getMinBuildHeight() + flyingDatum.flightTargetMinHeightOffset(),
         (double)this.bird.level().getMaxBuildHeight() - flyingDatum.flightTargetMaxHeightOffset()
      );
      return new Vec3(target.x, y, target.z);
   }

   public Vec3 randomHorizontalDirection() {
      return BirdFlightTargeting.randomHorizontalDirection(this.bird.getRandom());
   }

   public boolean canStartAmbientAirCruise() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
      return timer.getBirdFlyingTicker().getTicks() <= 0
         && this.bird.onGround()
         && this.bird.getRoutineController().isActiveTime()
         && this.bird.getNavigation().isDone()
         && !this.bird.getEatingController().isEating()
         && !this.bird.getRoutineController().isSleepingOrRoosting()
         && state != BirdBehaviorState.FORAGING
         && state != BirdBehaviorState.PERCHING
         && state != BirdBehaviorState.FOLLOWING
         && state != BirdBehaviorState.SENTINEL
         && !state.isEscape();
   }

   public void faceFlightDirection(Vec3 movement) {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      BirdFlightManager.faceMovement(this.bird, movement, flyingDatum.flightProfile().maxPitchDegrees());
   }

   public boolean shouldFaceGroundMovement() {
      if (this.bird.onGround() && !this.bird.isPassenger() && !this.bird.isInWater() && !this.bird.isVehicle()) {
         BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
         if (!state.isAirborne()
            && state != BirdBehaviorState.EATING
            && state != BirdBehaviorState.PREENING
            && state != BirdBehaviorState.DANCING
            && state != BirdBehaviorState.SLEEPING
            && state != BirdBehaviorState.ROOSTING) {
            BirdData birdData = this.bird.getBirdData();
            BirdMiscDatum miscDatum = birdData.misc();
            return this.bird.getDeltaMovement().lengthSqr() > miscDatum.walkingSpeedThreshold() || !this.bird.getNavigation().isDone();
         }
      }

      return false;
   }

   public void processLanding() {
      this.bird().setNoGravity(false);
      Vec3 currentDelta = this.bird().getDeltaMovement();
      BirdLandingTicker<? extends AbstractBirdEntity<?>> landingTicker = (BirdLandingTicker<? extends AbstractBirdEntity<?>>)this.bird()
         .getTickController()
         .getTickTimer()
         .getBirdLandingTicker();
      boolean enableLifecycleLog = landingTicker.enableLifecycleLog();
      double newX = currentDelta.x * 0.9995;
      double newZ = currentDelta.z * 0.9995;
      double newY = currentDelta.y * 1.08;
      if (!this.isLandingAdjusted) {
         BirdFlyingDatum flyingDatum = this.bird().getBirdData().flying();
         double damping = flyingDatum.flightLandingHorizontalDamping();
         double vi = flyingDatum.flightLandingMinimumInitialSpeed();
         double currentSpeed = Math.sqrt(newX * newX + newZ * newZ);
         if (currentSpeed < vi) {
            int n = (int)Math.ceil(Math.log(vi / currentSpeed) / Math.log(damping));
            double pow = Math.pow(damping, (double)Math.min(n, 20));
            newX *= pow;
            newZ *= pow;
         }

         this.isLandingAdjusted = true;
      }

      if (currentDelta.y >= -0.01) {
         newY = -0.01;
      }

      BlockPos pos;
      if (this.isMountFlight) {
         pos = this.findDryLandingSurfaceInAirWithBias(this.bird().blockPosition(), 3, 1);
      } else {
         pos = this.findDryLandingSurfaceInAirWithBias(this.bird().blockPosition(), 3, 0);
      }

      boolean found = pos != null;
      if (!found) {
         if (enableLifecycleLog) {
            BirdCameraMod.LOGGER.info("[Controller] Flying: Bird land failed, restart flying");
         }

         landingTicker.setTicks(0);
         this.isMountFlight = false;
         if (!this.bird().isBaby()) {
            this.startShortFlight(null, true);
         }
      }

      if (this.bird().onGround()) {
         newY = 0.0;
         this.isMountFlight = false;
         landingTicker.setTicks(0);
      } else {
         this.bird.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 2, 0, false, false));
      }

      this.bird().setDeltaMovement(new Vec3(newX, newY, newZ));
   }

   private BlockPos findDryLandingSurfaceInAirWithBias(BlockPos center, int verticalRange, int bias) {
      MutableBlockPos mutable = new MutableBlockPos();

      for (int yOffset = 0; yOffset >= -verticalRange; yOffset--) {
         mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
         if (this.isSafeDryLandingOrAir(mutable)) {
            return mutable.immutable();
         }

         for (int radius = 1; radius <= bias; radius++) {
            for (int x = -radius; x <= radius; x++) {
               for (int z = -radius; z <= radius; z++) {
                  if (Math.abs(x) == radius || Math.abs(z) == radius) {
                     mutable.set(center.getX() + x, center.getY() + yOffset, center.getZ() + z);
                     if (this.isSafeDryLandingOrAir(mutable)) {
                        return mutable.immutable();
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private BlockPos findDryLandingSurfaceInAir(BlockPos center, int verticalRange) {
      return this.findDryLandingSurfaceInAirWithBias(center, verticalRange, 0);
   }

   public boolean isSafeDryLandingOrAir(BlockPos pos) {
      Level level = this.bird().level();
      if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
         return false;
      } else {
         BlockState below = level.getBlockState(pos.below());
         BlockState feet = level.getBlockState(pos);
         BlockState head = level.getBlockState(pos.above());
         if (feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty()) {
            if (level.getFluidState(pos).is(FluidTags.WATER)
               || level.getFluidState(pos).is(FluidTags.LAVA)
               || level.getFluidState(pos.below()).is(FluidTags.WATER)
               || level.getFluidState(pos.below()).is(FluidTags.LAVA)) {
               return false;
            } else {
               return !below.is(Blocks.WATER) && !below.is(Blocks.LAVA)
                  ? below.isAir()
                     || below.isFaceSturdy(level, pos.below(), Direction.UP)
                     || below.is(BirdCameraBlockTags.BIRD_PERCHES)
                     || below.is(BlockTags.FENCES)
                     || below.is(BlockTags.WALLS)
                     || below.is(BlockTags.LEAVES)
                     || below.is(BlockTags.DIRT)
                     || below.is(BlockTags.SAND)
                     || below.is(Blocks.FARMLAND)
                     || below.is(Blocks.GRASS_BLOCK)
                     || below.is(Blocks.PODZOL)
                     || below.is(Blocks.MYCELIUM)
                     || below.getBlock() instanceof FenceBlock
                     || below.getBlock() instanceof FenceGateBlock
                  : false;
            }
         } else {
            return false;
         }
      }
   }

   public boolean isLandingAdjusted() {
      return this.isLandingAdjusted;
   }

   public void setLandingAdjusted(boolean landingAdjusted) {
      this.isLandingAdjusted = landingAdjusted;
   }
}
