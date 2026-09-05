package com.birdcamera.content.bird.core.controller.goal;

import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import net.minecraft.world.phys.Vec3;

public class BirdFlockGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private Vec3 flockTarget;

   @Override
   public int chance() {
      return this.goalDatum().flockChance();
   }

   @Override
   public boolean canUse() {
      return super.canUse() && !this.bird().getFlyingController().isFlightInProgress() && !this.bird().isBaby();
   }

   @Override
   public boolean onUse() {
      BirdGoalDatum goalDatum = this.bird().getBirdData().goal();
      List<BudgerigarEntity> flock = this.bird()
         .level()
         .getEntitiesOfClass(
            BudgerigarEntity.class, this.bird().getBoundingBox().inflate(goalDatum.flockSearchRange()), e -> e != this.bird() && !e.isPassenger()
         );
      if (flock.size() < goalDatum.flockMinSize()) {
         return false;
      } else {
         Vec3 center = Vec3.ZERO;

         for (BudgerigarEntity member : flock) {
            center = center.add(member.position());
         }

         center = center.scale(1.0 / (double)flock.size());
         double distance = this.bird().distanceToSqr(center);
         if (distance < goalDatum.flockTargetRange()) {
            return false;
         } else {
            this.flockTarget = center.add(
               (this.bird().getRandom().nextDouble() - 0.5) * goalDatum.flockRange(),
               0.0,
               (this.bird().getRandom().nextDouble() - 0.5) * goalDatum.flockRange()
            );
            return true;
         }
      }
   }

   @Override
   public boolean onContinue() {
      return this.flockTarget != null && this.bird().distanceToSqr(this.flockTarget) > this.goalDatum().flockLostRange();
   }

   @Override
   public void onStart() {
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.IDLE
         || this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
      }
   }

   @Override
   public void onReset() {
      if (this.flockTarget != null) {
         this.bird().getNavigation().moveTo(this.flockTarget.x, this.flockTarget.y, this.flockTarget.z, this.goalDatum().flockMoveSpeed());
      }
   }

   @Override
   public void onStop() {
      this.flockTarget = null;
   }
}
