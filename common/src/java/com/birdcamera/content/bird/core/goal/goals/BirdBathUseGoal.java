package com.birdcamera.content.bird.core.goal.goals;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdBathUseGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;

public class BirdBathUseGoal extends AbstractBirdGoal {
   public BirdBathUseGoal(AbstractBirdEntity<?> bird) {
      super(bird);
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdBathUseGoalController();
   }

   @Override
   protected void onStart() {
      super.onStart();
      this.resetTicks();
   }

   @Override
   protected void onReset() {
      super.onReset();
      this.resetTicks();
   }

   @Override
   protected boolean defaultContinuePredicates() {
      return this.bird().getBehaviorStateController().getBehaviorState().isEscape()
         || this.bird().getEatingController().isForagingOrEating()
         || super.defaultContinuePredicates();
   }

   private void resetTicks() {
      if (this.individualGoalController() instanceof BirdBathUseGoalController<?> birdBathUseGoalController && birdBathUseGoalController.bathExists()) {
         this.setRepathTicksWithVariance(this.goalDatum().bathUseStartTicks(), this.goalDatum().bathUseStartTicksVariance());
      }
   }
}
