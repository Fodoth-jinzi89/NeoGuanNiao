package com.birdcamera.content.bird.core.controller.goal;

import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdRoutineController;
import com.birdcamera.content.bird.core.controller.BirdTickController;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import net.minecraft.world.entity.item.ItemEntity;

public class BirdEatFoodGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private ItemEntity targetFood;

   @Override
   public int chance() {
      return this.goalDatum().eatFoodChance();
   }

   @Override
   public boolean canUse() {
      BirdTickController<? extends AbstractBirdEntity<?>> tickController = (BirdTickController<? extends AbstractBirdEntity<?>>)this.bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdEatingController<? extends AbstractBirdEntity<?>> eatingController = (BirdEatingController<? extends AbstractBirdEntity<?>>)this.bird
         .getEatingController();
      BirdRoutineController<? extends AbstractBirdEntity<?>> routineController = (BirdRoutineController<? extends AbstractBirdEntity<?>>)this.bird
         .getRoutineController();
      BirdBehaviorStateController<? extends AbstractBirdEntity<?>> stateController = (BirdBehaviorStateController<? extends AbstractBirdEntity<?>>)this.bird
         .getBehaviorStateController();
      boolean hasNoFoodTicks = !timer.getBirdFoodTicker().isRunning();
      boolean isNotEating = !eatingController.isEating();
      boolean isNotPassenger = !this.bird.isPassenger();
      boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
      boolean isNotEscaping = !stateController.getBehaviorState().isEscape();
      boolean isMature = !this.bird().isBaby();
      return isMature && hasNoFoodTicks && isNotEating && isNotPassenger && isNotSleepingOrRoosting && isNotEscaping;
   }

   @Override
   public boolean onUse() {
      List<ItemEntity> items = this.bird()
         .level()
         .getEntitiesOfClass(
            ItemEntity.class,
            this.bird().getBoundingBox().inflate(this.bird().getBirdData().goal().eatFoodSearchRange()),
            item -> !item.getItem().isEmpty() && this.bird().getEatingController().isEdibleFood(item.getItem())
         );
      if (items.isEmpty()) {
         return false;
      } else {
         this.targetFood = items.getFirst();
         return true;
      }
   }

   @Override
   public boolean canContinue() {
      if (!this.bird().getAnimationController().shouldPlayFlyAnimation() && !this.bird().onGround()) {
         this.targetFood = null;
      }

      return this.targetFood != null && this.targetFood.isAlive() && !this.targetFood.getItem().isEmpty() && this.canUse();
   }

   @Override
   public void onStart() {
      this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FORAGING);
   }

   @Override
   public void onTick() {
      if (this.targetFood != null && this.targetFood.isAlive()) {
         double distance = this.bird().distanceToSqr(this.targetFood);
         if (distance > 0.8 * this.goalDatum().eatFoodConsumeDistance()) {
            if (this.bird().getY() >= this.targetFood.getY()) {
               this.bird().getNavigation().setCanFloat(false);
            }

            this.bird().getNavigation().moveTo(this.targetFood, this.goalDatum().eatFoodMoveSpeed());
         }

         if (distance < this.goalDatum().eatFoodConsumeDistance()) {
            this.bird().getNavigation().stop();
            this.bird().getEatingController().consumeItemEntity(this.bird(), this.targetFood);
            this.targetFood = null;
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
         } else {
            this.bird().getLookControl().setLookAt(this.targetFood, this.goalDatum().eatFoodLookYaw(), this.goalDatum().eatFoodLookPitch());
         }
      }
   }

   @Override
   public void onStop() {
      this.targetFood = null;
      if (this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FORAGING) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
      }
   }
}
