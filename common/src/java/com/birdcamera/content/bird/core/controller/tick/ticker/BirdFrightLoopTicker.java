package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.datum.BirdFrightDatum;

public class BirdFrightLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdFrightLoopTicker() {
      super(true, false, true);
   }

   @Override
   protected void reset() {
      super.reset();
      BirdFrightDatum frightData = this.bird().getBirdData().fright();
      this.setTicks(frightData.frightCheckTicks() + this.bird().getRandom().nextInt(frightData.frightCheckTicksVariance()));
   }

   @Override
   protected void onReset() {
      if (this.bird().getFrightController().shouldFlee()) {
         BirdFrightDatum frightData = this.bird().getBirdData().fright();
         this.bird()
            .getFrightController()
            .frightenFrom(frightData.frightenFromTicks() + this.bird().getRandom().nextInt(frightData.frightenFromTicksVariance()));
      }
   }
}
