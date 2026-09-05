package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BirdIdleGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private BlockPos targetPos;

   @Override
   public int chance() {
      return this.goalDatum().idleChance();
   }

   @Override
   public boolean canUse() {
      return super.canUse() && !this.bird().getFlyingController().isFlightInProgress() && !this.bird().isBaby();
   }

   @Override
   public boolean onUse() {
      if (this.bird().getRandom().nextInt(this.goalDatum().idleRetargetChance()) != 0) {
         return false;
      } else {
         BirdBehaviorState state = this.bird().getBehaviorStateController().getBehaviorState();
         return state != BirdBehaviorState.IDLE && state != BirdBehaviorState.SENTINEL ? false : this.findTargetPosition();
      }
   }

   @Override
   public boolean onContinue() {
      return this.targetPos != null && this.bird().distanceToSqr(Vec3.atCenterOf(this.targetPos)) > this.goalDatum().idleStopDistance();
   }

   @Override
   public void onStart() {
      if (this.bird().getBehaviorStateController().getBehaviorState() != BirdBehaviorState.WALKING) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
      }
   }

   @Override
   public void onTick() {
      if (this.targetPos != null) {
         if (this.bird().getRandom().nextInt(this.goalDatum().idleLookAroundChance()) == 0) {
            float yaw = this.bird().getYRot() + (this.bird().getRandom().nextFloat() - 0.5F) * (float)this.goalDatum().idleLookAroundChance();
            this.bird().setYRot(yaw);
            this.bird().yBodyRot = yaw;
         }
      }
   }

   @Override
   public void onReset() {
      if (this.targetPos != null) {
         if (this.bird().getY() >= (double)this.targetPos.getY()) {
            this.bird().getNavigation().setCanFloat(false);
         }

         this.bird()
            .getNavigation()
            .moveTo((double)this.targetPos.getX() + 0.5, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + 0.5, this.goalDatum().idleMoveSpeed());
      }
   }

   @Override
   public void onStop() {
      this.targetPos = null;
      this.bird().getNavigation().stop();
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }

   private boolean findTargetPosition() {
      BlockPos origin = this.bird().blockPosition();
      int xRange = this.goalDatum().idleFindTargetXRange();
      int yRange = this.goalDatum().idleFindTargetYRange();
      int zRange = this.goalDatum().idleFindTargetZRange();
      int minY = origin.getY() - yRange / 2;
      int maxY = origin.getY() + yRange / 2;

      for (int attempt = 0; attempt < this.goalDatum().idleFindTargetMaxAttempts(); attempt++) {
         int x = origin.getX() + this.randomOffset(this.bird().getRandom(), xRange, this.goalDatum().idleFindTargetMinRange());
         int z = origin.getZ() + this.randomOffset(this.bird().getRandom(), zRange, this.goalDatum().idleFindTargetMinRange());

         for (int y = maxY; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockState state = this.bird().level().getBlockState(groundPos);
            if (!state.getCollisionShape(this.bird().level(), groundPos).isEmpty()) {
               BlockPos targetPos = groundPos.above();
               if (this.bird().getFlyingController().isSafeDryLandingOrAir(targetPos)) {
                  this.targetPos = targetPos;
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int randomOffset(RandomSource random, int range, int minDistance) {
      int half = range / 2;
      return random.nextBoolean() ? -half + random.nextInt(half - minDistance + 1) : minDistance + random.nextInt(half - minDistance + 1);
   }
}
