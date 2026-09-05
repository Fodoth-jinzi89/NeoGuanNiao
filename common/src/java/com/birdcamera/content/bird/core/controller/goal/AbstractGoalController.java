package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.AbstractBirdController;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;

public abstract class AbstractGoalController<T extends AbstractBirdEntity<?>> extends AbstractBirdController<T> {
   public int chance() {
      return 60;
   }

   public boolean canUse() {
      return this.defaultAdditionalPredicates();
   }

   public boolean canContinue() {
      return this.canUse();
   }

   public boolean onUse() {
      return true;
   }

   public boolean onContinue() {
      return true;
   }

   public void onStart() {
   }

   public boolean shouldTick() {
      return true;
   }

   public void onTick() {
   }

   public void onReset() {
   }

   public void onStop() {
   }

   protected BirdGoalDatum goalDatum() {
      return this.bird().getBirdData().goal();
   }

   public boolean defaultAdditionalPredicates() {
      return this.bird().getRoutineController().isActiveTime()
         && !this.bird().getEatingController().isEating()
         && !this.bird().isDancing()
         && !this.bird().getRoutineController().isSleepingOrRoosting()
         && !this.bird().getBehaviorStateController().getBehaviorState().isEscape();
   }
}
