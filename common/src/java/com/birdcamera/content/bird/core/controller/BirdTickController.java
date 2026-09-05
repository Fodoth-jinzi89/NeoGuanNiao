package com.birdcamera.content.bird.core.controller;

import java.util.function.Consumer;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.controller.tick.ticker.AbstractBirdTicker;

public class BirdTickController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private final BirdTickTimer<T> TICK_TIMER = new BirdTickTimer<>();

   @Override
   protected void onAttach() {
      this.TICK_TIMER.attach(this.bird());
   }

   @Override
   public void tick() {
      this.TICK_TIMER.tick();
   }

   public void tickClient() {
      this.TICK_TIMER.tickClient();
   }

   public BirdTickTimer<?> getTickTimer() {
      return this.TICK_TIMER;
   }

   public void forEachTicker(Consumer<AbstractBirdTicker<T>> consumer) {
      this.TICK_TIMER.forEachTicker(consumer);
   }
}
