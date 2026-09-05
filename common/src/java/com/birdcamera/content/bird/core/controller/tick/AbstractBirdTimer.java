package com.birdcamera.content.bird.core.controller.tick;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public abstract class AbstractBirdTimer<T extends AbstractBirdEntity<?>> {
   protected T bird;

   public final void attach(T bird) {
      if (this.bird != null) {
         throw new IllegalStateException("Ticker is already attached");
      } else {
         this.bird = bird;
         this.onAttach();
      }
   }

   protected void onAttach() {
   }

   protected final T bird() {
      if (this.bird == null) {
         throw new IllegalStateException("Ticker is not attached");
      } else {
         return this.bird;
      }
   }

   public void tick() {
   }

   public void tickClient() {
   }
}
