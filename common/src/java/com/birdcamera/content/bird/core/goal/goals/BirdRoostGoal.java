package com.birdcamera.content.bird.core.goal.goals;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;

public class BirdRoostGoal extends AbstractBirdGoal {
   public BirdRoostGoal(AbstractBirdEntity<?> bird) {
      super(bird);
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdRoostGoalController();
   }

   @Override
   protected boolean defaultContinuePredicates() {
      return this.bird().getRoutineController().isRoosting() || super.defaultContinuePredicates();
   }
}
