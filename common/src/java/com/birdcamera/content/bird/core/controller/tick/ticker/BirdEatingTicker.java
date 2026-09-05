package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;

public class BirdEatingTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   @Override
   protected void run() {
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      this.bird().getNavigation().stop();
      stateController.setBehaviorState(BirdBehaviorState.EATING);
   }

   @Override
   protected void onExpire() {
      super.onExpire();
      this.bird().getEatingController().clearEating();
   }
}
