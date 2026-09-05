package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdMusicTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdMusicTicker() {
      super(true, false);
   }
}
