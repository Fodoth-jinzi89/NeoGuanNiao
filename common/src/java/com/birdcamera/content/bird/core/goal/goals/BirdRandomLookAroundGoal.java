package com.birdcamera.content.bird.core.goal.goals;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;

public class BirdRandomLookAroundGoal extends AbstractBirdGoal {
   public BirdRandomLookAroundGoal(AbstractBirdEntity<?> bird) {
      super(bird);
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdRandomLookAroundGoalController();
   }

   @Override
   protected boolean defaultContinuePredicates() {
      return !this.bird().getRoutineController().isSleeping();
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   protected void reset() {
      this.setRepathTicksWithVariance(this.goalDatum().randomLookAroundTicks(), this.goalDatum().randomLookAroundTicksVariance());
   }

   @Override
   protected void debugStart() {
   }

   @Override
   protected void debugStop() {
   }
}
