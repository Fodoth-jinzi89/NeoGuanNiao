package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdLandingTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdLandingTicker() {
      super(true, false);
   }

   @Override
   protected void run() {
      super.run();
      T bird = this.bird();
      boolean onGround = bird.onGround();
      boolean isSleepingOrRoosting = this.bird().getRoutineController().isSleepingOrRoosting();
      if (!onGround && !isSleepingOrRoosting) {
         this.bird().getFlyingController().processLanding();
      } else {
         if (this.enableLifecycleLog()) {
            BirdCameraMod.LOGGER.info("[Ticker] Landing: Bird on ground = {}, sleeping or roosting = {}", onGround, isSleepingOrRoosting);
         }

         this.setTicks(0);
         bird.getTickController().getTickTimer().getBirdBehaviorStateTicker().setTicks(5);
      }
   }

   @Override
   public void setTicks(int ticks) {
      if (this.bird().onGround() && ticks > 0) {
         if (this.enableLifecycleLog()) {
            BirdCameraMod.LOGGER.warn("[Ticker] Trying to set Landing on ground! Forbidden");
         }
      } else {
         super.setTicks(ticks);
      }
   }

   @Override
   protected void onExpire() {
      this.bird().getFlyingController().setLandingAdjusted(false);
   }
}
