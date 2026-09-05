package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdRoutineController;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdCuriousTicker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class BirdCuriousFollowGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private LivingEntity curiousFollowTarget;

   @Override
   public int chance() {
      return this.goalDatum().curiousFollowChance();
   }

   @Override
   public boolean canUse() {
      return !this.bird().isBaby()
         && !this.bird().isTame()
         && !this.bird().isLeashed()
         && this.canStartSocialGoal()
         && this.bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning();
   }

   @Override
   public boolean onUse() {
      AABB searchBox = this.bird().getBoundingBox().inflate(this.bird().getBirdData().goal().curiousFollowSearchRange());

      for (LivingEntity e : this.bird().level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
         if (e instanceof Player player && !player.isSpectator() && player.isAlive()) {
            this.setCuriousFollowTarget(player);
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canContinue() {
      return this.getCuriousFollowTarget() != null
         && this.getCuriousFollowTarget().isAlive()
         && this.bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning()
         && this.canStartSocialGoal()
         && this.bird().distanceToSqr(this.getCuriousFollowTarget()) < this.bird().getBirdData().goal().curiousFollowLostRange();
   }

   public boolean canStartSocialGoal() {
      BirdRoutineController<? extends AbstractBirdEntity<?>> routineController = (BirdRoutineController<? extends AbstractBirdEntity<?>>)this.bird
         .getRoutineController();
      BirdEatingController<? extends AbstractBirdEntity<?>> eatingController = (BirdEatingController<? extends AbstractBirdEntity<?>>)this.bird
         .getEatingController();
      BirdBehaviorStateController<? extends AbstractBirdEntity<?>> stateController = (BirdBehaviorStateController<? extends AbstractBirdEntity<?>>)this.bird
         .getBehaviorStateController();
      boolean isActiveTime = routineController.isActiveTime();
      boolean isNotEating = !eatingController.isEating();
      boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
      boolean isNotEscaping = !stateController.getBehaviorState().isEscape();
      return isActiveTime && isNotEating && isNotSleepingOrRoosting && isNotEscaping;
   }

   @Override
   public void onStart() {
      BirdCuriousTicker<? extends AbstractBirdEntity<?>> curiousTicker = (BirdCuriousTicker<? extends AbstractBirdEntity<?>>)this.bird()
         .getTickController()
         .getTickTimer()
         .getBirdCuriousTicker();
      curiousTicker.setTicks(this.goalDatum().curiousTicks() + this.bird().getRandom().nextInt(this.goalDatum().curiousTicksVariance()));
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.CURIOUS);
      this.bird().getNavigation().stop();
   }

   @Override
   public void onTick() {
      if (this.getCuriousFollowTarget() != null) {
         this.bird().getLookControl().setLookAt(this.getCuriousFollowTarget(), this.goalDatum().curiousLookYaw(), this.goalDatum().curiousLookPitch());
         double distance = this.bird().distanceToSqr(this.getCuriousFollowTarget());
         if (distance > this.goalDatum().curiousFollowSneakRange()) {
            if (this.bird().getRandom().nextFloat() < this.goalDatum().curiousSneakChance()) {
               this.bird().getNavigation().moveTo(this.getCuriousFollowTarget(), this.goalDatum().curiousFollowSneakSpeed());
            } else {
               this.bird().getNavigation().stop();
            }
         }
      }
   }

   @Override
   public void onReset() {
      if (this.getCuriousFollowTarget() != null) {
         double distance = this.bird().distanceToSqr(this.getCuriousFollowTarget());
         if (distance > this.goalDatum().curiousFollowWalkRange()) {
            this.bird().getNavigation().moveTo(this.getCuriousFollowTarget(), this.goalDatum().curiousFollowWalkSpeed());
         }
      }
   }

   @Override
   public void onStop() {
      this.setCuriousFollowTarget(null);
      this.bird().getNavigation().stop();
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.CURIOUS) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }

   public LivingEntity getCuriousFollowTarget() {
      return this.curiousFollowTarget;
   }

   public void setCuriousFollowTarget(LivingEntity curiousFollowTarget) {
      this.curiousFollowTarget = curiousFollowTarget;
   }
}
