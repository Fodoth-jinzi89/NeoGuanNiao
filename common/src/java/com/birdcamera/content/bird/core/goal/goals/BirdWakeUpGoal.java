package com.birdcamera.content.bird.core.goal.goals;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;

public class BirdWakeUpGoal extends AbstractBirdGoal {
   public BirdWakeUpGoal(AbstractBirdEntity<?> bird) {
      super(bird);
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdWakeUpGoalController();
   }

   @Override
   protected boolean defaultContinuePredicates() {
      return this.bird().getRoutineController().isSleeping() || super.defaultContinuePredicates();
   }
}
