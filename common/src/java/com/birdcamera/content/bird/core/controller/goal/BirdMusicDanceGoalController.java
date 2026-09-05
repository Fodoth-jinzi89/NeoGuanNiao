package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;

public class BirdMusicDanceGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Override
   public int chance() {
      return this.goalDatum().musicDanceChance();
   }

   @Override
   public boolean canUse() {
      return super.canUse() && this.bird().getTickController().getTickTimer().getBirdMusicTicker().isRunning();
   }

   @Override
   public void onStart() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.DANCING);
      this.bird().getNavigation().stop();
   }

   @Override
   public boolean defaultAdditionalPredicates() {
      return this.bird().isDancing() || super.defaultAdditionalPredicates();
   }

   @Override
   public void onStop() {
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }
}
