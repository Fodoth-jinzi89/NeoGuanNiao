package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdFoodTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdFoodTicker() {
      super(true, false);
   }
}
