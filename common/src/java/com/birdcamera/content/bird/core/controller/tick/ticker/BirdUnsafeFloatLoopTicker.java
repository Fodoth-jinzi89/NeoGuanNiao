package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;

public class BirdUnsafeFloatLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdUnsafeFloatLoopTicker() {
      super(true, false, true);
   }

   @Override
   protected void reset() {
      super.reset();
      this.setTicks(20);
      this.processUnsafeFloating();
   }

   private void processUnsafeFloating() {
      T bird = this.bird();
      boolean isUnsafeFloating = !bird.onGround()
         && !bird.isFlying()
         && bird.getBehaviorStateController().getBehaviorState().isUnsafeFloatTickerEnabled()
         && !this.bird().getGoalController().getBirdBathUseGoalController().isRunning();
      if (this.enableLifecycleLog() && (double)this.bird().getRandom().nextFloat() <= 0.1) {
         BirdCameraMod.LOGGER
            .info(
               "[Ticker] UnsafeFloat: Bird unsafe floating check! NotFlying: {}, UnsafeFloatTickerEnabled: {}",
               !bird.isFlying(),
               bird.getBehaviorStateController().getBehaviorState().isUnsafeFloatTickerEnabled()
            );
      }

      if (isUnsafeFloating
         || this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FOLLOWING && this.bird().getDeltaMovement().length() < 1.0E-4) {
         bird.setNoGravity(false);
         BirdMiscDatum miscDatum = this.bird().getBirdData().misc();
         int cooldownTicks = bird.isTame()
            ? miscDatum.tameCooldownMin() + bird.getRandom().nextInt(miscDatum.tameCooldownVariance())
            : miscDatum.wildCooldownMin() + bird.getRandom().nextInt(miscDatum.wildCooldownVariance());
         bird.getFlyingController().setLandingAdjusted(true);
         int landingTicks = this.getTicks() + cooldownTicks;
         bird.getTickController().getTickTimer().getBirdLandingTicker().setTicks(Math.min((int)((double)landingTicks * 1.2), cooldownTicks * 5));
      }
   }

   @Override
   protected void onReset() {
   }
}
