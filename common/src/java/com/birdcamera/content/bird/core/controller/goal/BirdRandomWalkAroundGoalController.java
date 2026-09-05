package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class BirdRandomWalkAroundGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   @Override
   public int chance() {
      return this.goalDatum().randomWalkAroundChance();
   }

   @Override
   public boolean canUse() {
      BirdBehaviorState state = this.bird().getBehaviorStateController().getBehaviorState();
      return (state == BirdBehaviorState.IDLE || state == BirdBehaviorState.SENTINEL) && super.canUse() && !this.bird().hasControllingPassenger();
   }

   @Override
   public boolean onUse() {
      Vec3 vec3 = DefaultRandomPos.getPos(this.bird(), this.goalDatum().randomWalkAroundHorizontalRange(), this.goalDatum().randomWalkAroundVerticalRange());
      if (vec3 == null) {
         return false;
      } else {
         this.wantedX = vec3.x;
         this.wantedY = vec3.y;
         this.wantedZ = vec3.z;
         return super.onUse();
      }
   }

   @Override
   public boolean canContinue() {
      return !this.bird().getNavigation().isDone() && !this.bird().hasControllingPassenger() && this.defaultAdditionalPredicates();
   }

   @Override
   public void onStart() {
      this.bird().getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.goalDatum().randomWalkAroundSpeedModifier());
   }

   @Override
   public void onStop() {
      this.bird().getNavigation().stop();
   }
}
