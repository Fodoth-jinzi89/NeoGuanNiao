package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.registry.BirdCameraBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BirdRoostGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private BlockPos roostPos;

   @Override
   public int chance() {
      return this.goalDatum().randomLookAroundChance();
   }

   @Override
   public boolean canUse() {
      if (!this.shouldRoost()) {
         return false;
      } else {
         return !this.bird().getRoutineController().isSleeping() || !this.bird().isBaby() && !this.isGoodRoostPosition(true, this.bird().blockPosition())
            ? this.bird().getRandom().nextInt(this.goalDatum().roostReFindChance()) == 0
            : false;
      }
   }

   @Override
   public boolean onUse() {
      this.roostPos = this.findRoostPosition();
      return this.roostPos != null;
   }

   @Override
   public boolean canContinue() {
      if (!this.shouldRoost()) {
         return false;
      } else if (this.bird().isBaby()) {
         return false;
      } else {
         return this.bird().getRoutineController().isSleeping() && this.isGoodRoostPosition(false, this.bird().blockPosition()) ? false : this.roostPos != null;
      }
   }

   private boolean shouldRoost() {
      return this.bird().getRoutineController().isRoostTime()
         && !this.bird().getEatingController().isEating()
         && !this.bird().isDancing()
         && !this.bird().getBehaviorStateController().getBehaviorState().isEscape()
         && !this.bird().getTickController().getTickTimer().getBirdFrightTicker().isRunning()
         && !this.bird().getTickController().getTickTimer().getBirdPendingFrightTicker().isRunning();
   }

   @Override
   public void onStart() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.ROOSTING);
      this.bird().getNavigation().stop();
   }

   @Override
   public void onTick() {
      if (this.roostPos != null) {
         double distance = this.bird().distanceToSqr(Vec3.atCenterOf(this.roostPos));
         if (!this.bird().isBaby() && !(distance < this.goalDatum().roostGoalRange())) {
            this.bird()
               .getLookControl()
               .setLookAt(
                  (double)this.roostPos.getX() + 0.5,
                  (double)this.roostPos.getY(),
                  (double)this.roostPos.getZ() + 0.5,
                  this.goalDatum().roostLookYaw(),
                  this.goalDatum().roostLookPitch()
               );
         } else {
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            this.bird().setPos((double)this.roostPos.getX() + 0.5, (double)this.roostPos.getY() - 0.5, (double)this.roostPos.getZ() + 0.5);
            if (!this.bird().isBaby()) {
               this.bird().setNoGravity(true);
            }
         }
      }
   }

   @Override
   public void onReset() {
      if (this.roostPos != null && !this.bird().isBaby()) {
         this.bird()
            .getNavigation()
            .moveTo((double)this.roostPos.getX() + 0.5, (double)this.roostPos.getY(), (double)this.roostPos.getZ() + 0.5, this.goalDatum().roostMoveSpeed());
      }
   }

   @Override
   public void onStop() {
      this.roostPos = null;
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING) {
         if (!this.bird().getRoutineController().isRoosting() && !this.bird().isBaby()) {
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
         } else {
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
         }
      }
   }

   private BlockPos findRoostPosition() {
      BlockPos origin = this.bird().blockPosition();

      for (int attempt = 0; attempt < this.goalDatum().roostFindTargetMaxAttempts(); attempt++) {
         int x = origin.getX() + this.bird().getRandom().nextInt(this.goalDatum().roostFindTargetXRange()) - this.goalDatum().roostFindTargetXRange() / 2;
         int z = origin.getZ() + this.goalDatum().roostFindTargetZRange() - this.goalDatum().roostFindTargetZRange() / 2;
         int y = origin.getY() + this.bird().getRandom().nextInt(this.goalDatum().roostFindTargetYRange()) + this.goalDatum().roostFindTargetYOffset();
         BlockPos pos = new BlockPos(x, y, z);
         if (this.isGoodRoostPosition(true, pos)) {
            return pos;
         }
      }

      for (int attemptx = 0; attemptx < this.goalDatum().roostFallbackTargetMaxAttempts(); attemptx++) {
         int x = origin.getX()
            + this.bird().getRandom().nextInt(this.goalDatum().roostFallbackTargetXRange())
            - this.goalDatum().roostFallbackTargetXRange() / 2;
         int z = origin.getZ() + this.goalDatum().roostFallbackTargetZRange() - this.goalDatum().roostFallbackTargetZRange() / 2;
         int y = origin.getY() + this.bird().getRandom().nextInt(this.goalDatum().roostFallbackTargetYRange()) + this.goalDatum().roostFallbackTargetYOffset();
         BlockPos pos = new BlockPos(x, y, z);
         if (this.isGoodRoostPosition(false, pos)) {
            return pos;
         }
      }

      return null;
   }

   private boolean isGoodRoostPosition(boolean strict, BlockPos pos) {
      BlockState state = this.bird().level().getBlockState(pos);
      if (!strict) {
         return state.isAir() ? !this.bird().level().getBlockState(pos.below()).isAir() : false;
      } else if (state.getBlock() instanceof LeavesBlock) {
         BlockState block = this.bird().level().getBlockState(pos.below());
         return block.isAir() || block.getBlock() instanceof LeavesBlock;
      } else {
         return state.is(BirdCameraBlockTags.BIRD_PERCHES) || this.bird().level().getBlockState(pos.below()).is(BirdCameraBlockTags.BIRD_PERCHES);
      }
   }
}
