package com.birdcamera.content.bird.core;

import software.bernie.geckolib.animation.RawAnimation;

public enum BirdGuidePreviewAnimation {
   NONE(null);

   private final RawAnimation animation;

   private BirdGuidePreviewAnimation(RawAnimation animation) {
      this.animation = animation;
   }

   public RawAnimation animation() {
      return this.animation;
   }
}
