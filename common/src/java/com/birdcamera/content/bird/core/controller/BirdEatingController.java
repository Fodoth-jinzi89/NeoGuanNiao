package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bath.BirdBathBlockEntity;
import com.birdcamera.content.bath.BirdBathContentType;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdEatingDatum;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdTameDatum;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import com.birdcamera.registry.BirdCameraItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BirdEatingController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public boolean isEating() {
      BirdBehaviorState currentState = this.bird.getBehaviorStateController().getBehaviorState();
      return currentState == BirdBehaviorState.EATING;
   }

   public boolean isForagingOrEating() {
      BirdBehaviorState currentState = this.bird.getBehaviorStateController().getBehaviorState();
      return currentState == BirdBehaviorState.EATING || currentState == BirdBehaviorState.FORAGING;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      InteractionResult breedResult = this.bird().getBreedController().mobInteract(player, hand);
      if (breedResult.consumesAction()) {
         return breedResult;
      } else if (!this.isEdibleFood(stack)) {
         return InteractionResult.CONSUME;
      } else if (this.isEating()) {
         return InteractionResult.CONSUME;
      } else {
         if (this.bird() instanceof BudgerigarEntity budgerigar && budgerigar.isBusyWithMusicOrSleep()) {
            return InteractionResult.CONSUME;
         }

         if (this.bird().getRoutineController().isSleepingOrRoosting()) {
            return InteractionResult.CONSUME;
         } else if (this.bird().getFlyingController().isBirdFlightActive()) {
            return InteractionResult.CONSUME;
         } else if (this.bird().level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
         } else {
            ItemStack eaten = stack.copy();
            eaten.setCount(1);
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            BirdData data = this.bird().getBirdData();
            BirdTameDatum tameDatum = data.tame();
            this.bird().getTameController().checkTame(player, eaten, tameDatum.addTrustValue(), tameDatum.addTrustNearbyValue(), true);
            this.startEatingFood(eaten);
            this.accelerateBabyGrowth();
            return InteractionResult.SUCCESS;
         }
      }
   }

   private void accelerateBabyGrowth() {
      if (this.bird().isBaby()) {
         int age = this.bird().getAge();
         if (age < 0) {
            int growth = (int)((float)(-age) * 0.1F);
            this.bird().setAge(age + growth);
            if (this.bird().level() instanceof ServerLevel serverLevel) {
               serverLevel.sendParticles(
                  ParticleTypes.HAPPY_VILLAGER,
                  this.bird().getX(),
                  this.bird().getY() + (double)this.bird().getBbHeight() * 0.5,
                  this.bird().getZ(),
                  5,
                  0.2,
                  0.2,
                  0.2,
                  0.02
               );
            }
         }
      }
   }

   public void consumeItemEntity(AbstractBirdEntity<?> bird, ItemEntity itemEntity) {
      ItemStack stack = itemEntity.getItem();
      ItemStack eaten = stack.copy();
      eaten.setCount(1);
      stack.shrink(1);
      if (stack.isEmpty()) {
         itemEntity.discard();
      } else {
         itemEntity.setItem(stack);
      }

      BirdData data = this.bird().getBirdData();
      BirdTameDatum tameDatum = data.tame();
      BirdMiscDatum miscDatum = data.misc();
      BirdTickController<T> tickController = this.bird().getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      this.spawnFlyingFood(bird, itemEntity, eaten);
      this.startEatingFood(eaten);
      int trustAmount = (int)((float)tameDatum.addTrustValue() * miscDatum.droppedItemTrustMultiplier());
      trustAmount *= this.bird().isBaby() ? 2 : 1;
      timer.getBirdTrustTicker().addTrust(trustAmount);
      int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
      int curiousLimit = data.eating().curiousTicksLimitForDroppedFood();
      timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
      int nearbyTrustAmount = (int)((float)tameDatum.addTrustNearbyValue() * miscDatum.droppedItemTrustMultiplier());
      this.shareTrustNearby(nearbyTrustAmount);
   }

   private void spawnFlyingFood(AbstractBirdEntity<?> bird, ItemEntity itemEntity, ItemStack food) {
      ItemStack flyingStack = food.copy();
      flyingStack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag() {
         {
            this.putBoolean("BirdCameraModFlyingFood", true);
         }
      }));
      ItemEntity flyingFood = new ItemEntity(bird.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), flyingStack);
      flyingFood.setNeverPickUp();
      double dx = bird.getX() - flyingFood.getX();
      double dy = bird.getY() + 0.5 * (double)bird.getBbHeight() - flyingFood.getY();
      double dz = bird.getZ() - flyingFood.getZ();
      double speed = 0.24;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance > 0.0) {
         flyingFood.setDeltaMovement(dx / distance * speed, dy / distance * speed, dz / distance * speed);
      }

      bird.level().addFreshEntity(flyingFood);
   }

   public void shareTrustNearby(int amount) {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      double range = miscDatum.trustShareRange();

      for (AbstractBirdEntity<?> b : this.bird().level().getEntitiesOfClass(AbstractBirdEntity.class, this.bird().getBoundingBox().inflate(range))) {
         if (b != this.bird()) {
            BirdTickTimer<? extends AbstractBirdEntity<?>> targetTimer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)b.getTickController().getTickTimer();
            targetTimer.getBirdTrustTicker().addTrust(amount);
            int currentCuriousTicks = targetTimer.getBirdCuriousTicker().getTicks();
            int curiousLimit = miscDatum.curiousTicksLimitForSharedTrust();
            targetTimer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
         }
      }
   }

   public void startEatingFood(ItemStack foodStack) {
      BirdData birdData = this.bird().getBirdData();
      BirdEatingDatum eatingDatum = birdData.eating();
      int eatingTicks = eatingDatum.eatingTicks() + this.bird().getRandom().nextInt(eatingDatum.eatingTicksVariant());
      int foodTicks = eatingDatum.foodTicks() + this.bird().getRandom().nextInt(eatingDatum.foodTicksVariant());
      float volume = eatingDatum.eatSoundVolume() + this.bird().getRandom().nextFloat() * eatingDatum.eatSoundVolumeVariant();
      float pitch = eatingDatum.eatSoundPitch() + this.bird().getRandom().nextFloat() * eatingDatum.eatSoundPitchVariant();
      this.startEatingBehavior(eatingTicks, foodTicks, volume, pitch);
   }

   public void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
      BirdData birdData = this.bird().getBirdData();
      BirdEatingDatum eatingDatum = birdData.eating();
      int eatingTicks = eatingDatum.eatingTicks() + this.bird().getRandom().nextInt(eatingDatum.eatingTicksVariant());
      int foodTicks = eatingDatum.foodTicks() + this.bird().getRandom().nextInt(eatingDatum.foodTicksVariant());
      float volume = eatingDatum.eatSoundVolume() + this.bird().getRandom().nextFloat() * eatingDatum.eatSoundVolumeVariant();
      float pitch = eatingDatum.eatSoundPitch() + this.bird().getRandom().nextFloat() * eatingDatum.eatSoundPitchVariant();
      float multiplier = eatingDatum.eatBathMultiplier();
      this.startEatingBehavior(eatingTicks, foodTicks, volume, pitch, multiplier);
   }

   public void clearEating() {
      BirdBehaviorStateController<T> behaviorController = this.bird.getBehaviorStateController();
      if (behaviorController.getBehaviorState() == BirdBehaviorState.EATING) {
         behaviorController.setBehaviorState(BirdBehaviorState.IDLE);
      }
   }

   private void startEatingBehavior(int eatingTicks, int foodTicks, float volume, float pitch) {
      this.startEatingBehavior(eatingTicks, foodTicks, volume, pitch, 1.0F);
   }

   private void startEatingBehavior(int eatingTicks, int foodTicks, float volume, float pitch, float multiplier) {
      BirdTickController<T> tickController = this.bird().getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      int finalEatingTicks = (int)((float)eatingTicks * multiplier);
      int finalFoodTicks = (int)((float)foodTicks * multiplier);
      float finalVolume = volume * multiplier;
      float finalPitch = pitch * multiplier;
      this.bird().getNavigation().stop();
      timer.getBirdEatingTicker().setTicks(finalEatingTicks);
      timer.getBirdFoodTicker().setTicks(finalFoodTicks);
      this.bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.EATING, finalEatingTicks);
      this.bird().playSound(this.bird().getBirdData().sound().eatSound(), finalVolume, finalPitch);
   }

   public boolean isEdibleFood(ItemStack stack) {
      return stack.is(BirdCameraItemTags.BIRD_FOOD);
   }

   public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
      BirdBehaviorStateController<T> behaviorController = this.bird.getBehaviorStateController();
      this.bird().getNavigation().stop();
      BirdData birdData = this.bird().getBirdData();
      BirdEatingDatum eatingDatum = birdData.eating();
      if (contentType.isFood()) {
         behaviorController.setBehaviorStateFor(BirdBehaviorState.EATING, Math.max(eatingDatum.eatingTicksLimitForBath(), ticks));
      } else {
         int curiousLimit = Math.max(birdData.misc().curiousTicksLimitForBath(), ticks / 2);
         behaviorController.setBehaviorStateFor(BirdBehaviorState.CURIOUS, curiousLimit);
      }
   }
}
