package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;

public class BirdRandomLookAroundGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private double relX;
   private double relZ;

   @Override
   public int chance() {
      return this.goalDatum().randomLookAroundChance();
   }

   @Override
   public boolean canUse() {
      return !this.bird().getRoutineController().isSleeping();
   }

   @Override
   public void onStart() {
      double d0 = (Math.PI * 2) * this.bird().getRandom().nextDouble();
      this.relX = Math.cos(d0);
      this.relZ = Math.sin(d0);
   }

   @Override
   public void onTick() {
      this.bird().getLookControl().setLookAt(this.bird().getX() + this.relX, this.bird().getEyeY(), this.bird().getZ() + this.relZ);
   }
}
