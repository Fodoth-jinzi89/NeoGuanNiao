package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;

public class BirdSentinelTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdSentinelTicker() {
      super(true, true);
   }

   @Override
   protected void run() {
      if (!this.defaultAdditionalPredicates()) {
         this.setTicks(0);
      } else {
         BirdGoalDatum goalDatum = this.bird().getBirdData().goal();
         if (this.getTicks() % goalDatum.sentinelLookAroundInterval() == 0) {
            float yaw = this.bird().getYRot() + goalDatum.sentinelLookYawVariance() * (float)(this.bird().getRandom().nextBoolean() ? 1 : -1);
            this.bird().setYRot(yaw);
            this.bird().yBodyRot = yaw;
         }
      }
   }

   @Override
   protected void onExpire() {
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SENTINEL) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }

   @Override
   protected void onSet(int ticksOld, int ticksNew) {
   }

   private boolean defaultAdditionalPredicates() {
      return this.bird().getRoutineController().isActiveTime()
         && !this.bird().getEatingController().isEating()
         && !this.bird().isDancing()
         && !this.bird().getRoutineController().isSleepingOrRoosting()
         && !this.bird().getBehaviorStateController().getBehaviorState().isEscape();
   }
}
