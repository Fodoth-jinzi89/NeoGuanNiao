package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import net.minecraft.world.phys.Vec3;

public class BirdSentinelGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Override
   public int chance() {
      return this.goalDatum().randomLookAroundChance();
   }

   @Override
   public boolean canContinue() {
      return false;
   }

   @Override
   public void onStart() {
      this.bird()
         .getTickController()
         .getTickTimer()
         .getBirdSentinelTicker()
         .setTicksWithVariance(this.goalDatum().sentinelTicks(), this.goalDatum().sentinelTicks());
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SENTINEL);
      this.bird().getNavigation().stop();
      Vec3 lookAt = new Vec3(
         this.bird().getX() + this.bird().getRandom().nextGaussian() * this.goalDatum().sentinelLookXVariance(),
         this.bird().getY(),
         this.bird().getZ() + this.bird().getRandom().nextGaussian() * this.goalDatum().sentinelLookZVariance()
      );
      this.bird().getLookControl().setLookAt(lookAt.x, lookAt.y, lookAt.z, this.goalDatum().sentinelLookYaw(), this.goalDatum().sentinelLookPitch());
   }

   @Override
   public void onStop() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
   }
}
