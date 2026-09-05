package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdCuriousTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdCuriousTicker() {
      super(true, false);
   }
}
