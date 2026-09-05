package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdBehaviorStateTicker;

public class BirdBehaviorStateController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private BirdBehaviorState behaviorState = BirdBehaviorState.IDLE;

   @Override
   protected void onAttach() {
      super.onAttach();
      this.decodeBehaviorState();
   }

   public void decodeBehaviorState() {
      this.behaviorState = this.getBehaviorState();
   }

   public BirdBehaviorState getBehaviorState() {
      int ordinal = (Integer)this.bird().getEntityData().get(AbstractBirdEntity.BEHAVIOR_STATE);
      BirdBehaviorState[] values = BirdBehaviorState.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BirdBehaviorState.IDLE;
   }

   public void setBehaviorState(BirdBehaviorState state) {
      BirdBehaviorState targetState = state != null ? state : BirdBehaviorState.IDLE;
      this.behaviorState = targetState;
      this.bird().getEntityData().set(AbstractBirdEntity.BEHAVIOR_STATE, targetState.ordinal());
   }

   public void setBehaviorStateFor(BirdBehaviorState state, int ticks) {
      this.setBehaviorState(state);
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird().getTickController().getTickTimer();
      BirdBehaviorStateTicker<? extends AbstractBirdEntity<?>> stateTicker = (BirdBehaviorStateTicker<? extends AbstractBirdEntity<?>>)timer.getBirdBehaviorStateTicker();
      int currentTicks = stateTicker.getTicks();
      stateTicker.setTicks(Math.max(currentTicks, ticks));
   }

   public BirdBehaviorState getCachedBehaviorState() {
      return this.behaviorState;
   }
}
