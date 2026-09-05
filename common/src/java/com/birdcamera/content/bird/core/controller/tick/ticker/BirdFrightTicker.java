package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdFrightTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdFrightTicker() {
      super(true, false);
   }

   @Override
   protected void onExpire() {
      this.bird().getFrightController().setFrightSource(null);
   }
}
