package com.birdcamera.content.bird.core.goal.goals;

import java.util.EnumSet;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.goal.AbstractBirdGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class BirdFlockGoal extends AbstractBirdGoal {
   public BirdFlockGoal(AbstractBirdEntity<?> bird) {
      super(bird);
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   protected AbstractGoalController<?> individualGoalController() {
      return this.goalController().getBirdFlockGoalController();
   }
}
