package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdTickController;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;
import net.minecraft.util.RandomSource;

public class BirdPostTameActionTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   @Override
   protected void run() {
      BirdTickController<T> tickController = this.bird().getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      BirdEatingController<T> eatingController = this.bird().getEatingController();
      BirdData birdData = this.bird().getBirdData();
      RandomSource random = this.bird().getRandom();
      if (!this.bird().isPassenger()) {
         if (eatingController.isEating()) {
            eatingController.clearEating();
         }

         BirdBehaviorState currentState = stateController.getBehaviorState();
         if (currentState == BirdBehaviorState.SLEEPING || currentState == BirdBehaviorState.ROOSTING) {
            timer.getBirdBehaviorStateTicker().setTicks(0);
            stateController.setBehaviorState(BirdBehaviorState.CURIOUS);
         }

         if (this.bird().getOwner() != null && this.bird().tickCount % 8 == 0) {
            this.bird().getLookControl().setLookAt(this.bird().getOwner(), 35.0F, 35.0F);
         }

         BirdPostTameActionSwapTicker<? extends AbstractBirdEntity<?>> postTameSwapTicker = (BirdPostTameActionSwapTicker<? extends AbstractBirdEntity<?>>)timer.getBirdPostTameActionSwapTicker();
         boolean shouldSwitch = postTameSwapTicker.getTicks() <= 0 || currentState == BirdBehaviorState.IDLE;
         if (shouldSwitch) {
            BirdBehaviorState newState = random.nextBoolean() ? BirdBehaviorState.CURIOUS : BirdBehaviorState.PREENING;
            int baseTicks = birdData.tame().tamedBehaviorTicks();
            int variance = birdData.tame().tamedBehaviorTicksVariance();
            int behaviorTicks = baseTicks + random.nextInt(variance);
            stateController.setBehaviorStateFor(newState, behaviorTicks);
            int swapBase = birdData.tame().postTameActionSwapTicks();
            int swapVariance = birdData.tame().postTameActionSwapTicksVariance();
            postTameSwapTicker.setTicks(swapBase + random.nextInt(swapVariance));
         }
      }
   }
}
