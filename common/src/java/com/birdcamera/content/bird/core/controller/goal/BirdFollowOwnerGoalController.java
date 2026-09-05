package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.flight.BirdFlightTargeting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BirdFollowOwnerGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private LivingEntity owner;

   @Override
   public int chance() {
      return this.goalDatum().followOwnerChance();
   }

   @Override
   public boolean onUse() {
      if (!this.bird().isBaby() && this.bird().isTame()) {
         this.owner = this.bird().getOwner();
         return this.isOwnerValid(true);
      } else {
         return false;
      }
   }

   @Override
   public boolean onContinue() {
      return this.bird().getFlyingController().isFlightInProgress() || this.isOwnerValid(false);
   }

   @Override
   public void onStart() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);
   }

   @Override
   public void onTick() {
      if (this.owner != null) {
         this.bird().getLookControl().setLookAt(this.owner, this.goalDatum().followOwnerLookYaw(), (float)this.bird().getMaxHeadXRot());
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);
         double distanceSqr = this.bird().distanceToSqr(this.owner);
         if (!this.bird().getFlyingController().isFlightInProgress()
            && distanceSqr > this.goalDatum().followOwnerStartFlyDistance()
            && this.bird().onGround()
            && !this.bird().getTickController().getTickTimer().getBirdFlyingTicker().isRunning()) {
            Vec3 target = BirdFlightTargeting.findDryLandingTargetNear(
               this.bird(),
               this.owner.blockPosition(),
               this.goalDatum().followOwnerLandingHorizontalRange(),
               this.goalDatum().followOwnerLandingVerticalRange()
            );
            if (target != null) {
               this.bird().getFlyingController().startShortFlight(target, false);
            }
         }
      }
   }

   @Override
   public void onReset() {
      this.bird().getNavigation().moveTo(this.owner, this.goalDatum().followOwnerMoveSpeed());
   }

   @Override
   public void onStop() {
      this.owner = null;
      if (!this.bird().getFlyingController().isFlightInProgress() && this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FOLLOWING
         )
       {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }

   private boolean isOwnerValid(boolean start) {
      if (this.owner == null || !this.owner.isAlive()) {
         return false;
      } else {
         return start
            ? this.bird().distanceToSqr(this.owner) > this.goalDatum().followOwnerStartDistance()
            : this.bird().distanceToSqr(this.owner) > this.goalDatum().followOwnerStopDistance();
      }
   }
}
