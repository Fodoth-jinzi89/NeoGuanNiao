package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public abstract class AbstractBirdController<T extends AbstractBirdEntity<?>> {
   protected T bird;

   public final void attach(T bird) {
      if (this.bird != null) {
         throw new IllegalStateException("Controller is already attached to a bird entity");
      } else {
         this.bird = bird;
         this.onAttach();
      }
   }

   protected void onAttach() {
   }

   protected final T bird() {
      if (this.bird == null) {
         throw new IllegalStateException("Controller is not attached to a bird entity");
      } else {
         return this.bird;
      }
   }

   public void tick() {
   }

   public void onRemoved() {
   }
}
