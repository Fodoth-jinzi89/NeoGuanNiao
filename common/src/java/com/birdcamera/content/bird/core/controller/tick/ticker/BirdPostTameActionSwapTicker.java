package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;

public class BirdPostTameActionSwapTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   @Override
   protected void onExpire() {
      super.onExpire();
      BirdBehaviorStateTicker<? extends AbstractBirdEntity<?>> behaviorStateTicker = (BirdBehaviorStateTicker<? extends AbstractBirdEntity<?>>)this.bird()
         .getTickController()
         .getTickTimer()
         .getBirdBehaviorStateTicker();
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      BirdBehaviorState currentState = stateController.getBehaviorState();
      boolean isCuriousOrPreening = currentState == BirdBehaviorState.CURIOUS || currentState == BirdBehaviorState.PREENING;
      if (isCuriousOrPreening) {
         behaviorStateTicker.setTicks(0);
         stateController.setBehaviorState(BirdBehaviorState.IDLE);
      }
   }
}
