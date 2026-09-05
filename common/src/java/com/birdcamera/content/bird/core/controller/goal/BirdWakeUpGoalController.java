package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;

public class BirdWakeUpGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Override
   public int chance() {
      return this.goalDatum().wakeUpChance();
   }

   @Override
   public boolean canUse() {
      return this.bird().getRoutineController().isSleeping() && this.bird().getRoutineController().isActiveTime();
   }

   @Override
   public boolean canContinue() {
      return !this.bird().getRoutineController().isSleeping();
   }

   @Override
   public void onStart() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      this.bird().getNavigation().stop();
      if (!this.bird().isBaby() && !this.bird().isTame() && !this.bird().isLeashed()) {
         this.bird().getFlyingController().startShortFlight(null, false);
      }
   }
}
