package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;

public class BirdRoutineController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private static final long DAY_LENGTH = 24000L;

   public boolean isActiveTime() {
      BirdData birdData = this.bird.getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      long activeStart = miscDatum.activeStartTime();
      long activeEnd = miscDatum.activeEndTime();
      long time = this.bird.level().getDayTime() % 24000L;
      return time >= activeStart || time < activeEnd;
   }

   public boolean isRoostTime() {
      BirdData birdData = this.bird.getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      long activeStart = miscDatum.activeStartTime();
      long activeEnd = miscDatum.activeEndTime();
      long time = this.bird.level().getDayTime() % 24000L;
      return time >= activeEnd && time < activeStart;
   }

   public boolean isSleepingOrRoosting() {
      BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
      return state == BirdBehaviorState.SLEEPING || state == BirdBehaviorState.ROOSTING;
   }

   public boolean isSleeping() {
      BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
      return state == BirdBehaviorState.SLEEPING;
   }

   public boolean isRoosting() {
      BirdBehaviorState state = this.bird.getBehaviorStateController().getBehaviorState();
      return state == BirdBehaviorState.ROOSTING;
   }
}
