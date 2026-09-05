package com.birdcamera.content.bird.core;

import java.util.EnumSet;

public enum BirdBehaviorState {
   IDLE,
   WALKING,
   FORAGING,
   FOLLOWING,
   SENTINEL,
   ALERT,
   FLEEING,
   FLYING,
   ROOSTING,
   SLEEPING,
   EATING,
   PREENING,
   CURIOUS,
   DANCING,
   PERCHING,
   BATHING,
   USING_BATH;

   public boolean isAirborne() {
      return this == FLYING || this == FLEEING;
   }

   public boolean isEscape() {
      return this == FLEEING || this == ALERT;
   }

   public boolean isUnsafeFlyTickerEnabled() {
      return EnumSet.of(IDLE, SENTINEL, ALERT, PREENING, CURIOUS, DANCING, ROOSTING, SLEEPING).contains(this);
   }

   public boolean isUnsafeFloatTickerEnabled() {
      EnumSet<BirdBehaviorState> flyStates = EnumSet.of(IDLE, WALKING, SENTINEL, ALERT, PREENING, CURIOUS, DANCING, EATING);
      return flyStates.contains(this);
   }
}
