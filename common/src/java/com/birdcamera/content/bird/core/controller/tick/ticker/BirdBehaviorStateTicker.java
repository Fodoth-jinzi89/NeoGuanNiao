package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdRoutineController;
import com.birdcamera.content.bird.core.controller.BirdTickController;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;

public class BirdBehaviorStateTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   @Override
   protected void onExpire() {
      super.onExpire();
      BirdTickController<T> tickController = this.bird().getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      BirdEatingController<T> eatingController = this.bird().getEatingController();
      BirdRoutineController<T> routineController = this.bird().getRoutineController();
      BirdData birdData = this.bird().getBirdData();
      BirdBehaviorState currentState = stateController.getBehaviorState();
      BirdPostTameActionTicker<? extends AbstractBirdEntity<?>> postTameActionTicker = (BirdPostTameActionTicker<? extends AbstractBirdEntity<?>>)timer.getBirdPostTameActionTicker();
      boolean hasNoSpecialState = postTameActionTicker.getTicks() <= 0 && !eatingController.isEating();
      if (hasNoSpecialState) {
         boolean shouldSleep = routineController.isRoostTime() && this.bird().getNavigation().isDone();
         if (shouldSleep) {
            stateController.setBehaviorState(BirdBehaviorState.SLEEPING);
         } else if (currentState != BirdBehaviorState.FLEEING && currentState != BirdBehaviorState.FLYING) {
            boolean isTame = this.bird().isTame();
            boolean hasOwner = this.bird().getOwner() != null;
            boolean isNavigating = !this.bird().getNavigation().isDone();
            double distanceToOwnerSqr = hasOwner ? this.bird().distanceToSqr(this.bird().getOwner()) : 0.0;
            double followingThreshold = birdData.misc().followingDistanceThreshold();
            if (isTame && hasOwner && isNavigating && distanceToOwnerSqr > followingThreshold) {
               stateController.setBehaviorState(BirdBehaviorState.FOLLOWING);
            } else {
               double movementSpeedSqr = this.bird().getDeltaMovement().lengthSqr();
               double walkingThreshold = birdData.misc().walkingSpeedThreshold();
               boolean isMoving = movementSpeedSqr > walkingThreshold;
               boolean isDoneNavigating = this.bird().getNavigation().isDone();
               if (isMoving || !isDoneNavigating) {
                  stateController.setBehaviorState(BirdBehaviorState.WALKING);
               } else if (currentState == BirdBehaviorState.WALKING
                  || currentState == BirdBehaviorState.FORAGING
                  || currentState == BirdBehaviorState.FOLLOWING
                  || currentState == BirdBehaviorState.ALERT) {
                  stateController.setBehaviorState(BirdBehaviorState.IDLE);
               }
            }
         } else {
            stateController.setBehaviorState(BirdBehaviorState.ALERT);
         }
      }
   }
}
