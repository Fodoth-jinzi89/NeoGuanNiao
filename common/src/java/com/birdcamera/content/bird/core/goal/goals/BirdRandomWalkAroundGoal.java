package com.birdcamera.content.bird.core.goal.goals;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;

public class BirdRandomWalkAroundGoal extends AbstractBirdGoal {
   public BirdRandomWalkAroundGoal(AbstractBirdEntity<?> bird) {
      super(bird);
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdRandomWalkAroundGoalController();
   }

   @Override
   protected void debugStart() {
   }

   @Override
   protected void debugStop() {
   }
}
