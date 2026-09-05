package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import com.birdcamera.registry.BirdCameraItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BirdBreedController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      InteractionResult foodBagResult = this.bird().getFoodBagController().mobInteract(player, hand);
      if (foodBagResult.consumesAction()) {
         return foodBagResult;
      } else if (!this.isBreedingFood(stack)) {
         return InteractionResult.PASS;
      } else if (this.bird().isBaby()) {
         return InteractionResult.PASS;
      } else if (this.bird().getAge() > 0) {
         return InteractionResult.PASS;
      } else if (this.bird().getEatingController().isEating()) {
         return InteractionResult.PASS;
      } else {
         if (this.bird() instanceof BudgerigarEntity b && b.isBusyWithMusicOrSleep()) {
            return InteractionResult.PASS;
         }

         if (this.bird().getRoutineController().isSleepingOrRoosting() || this.bird().getFlyingController().isBirdFlightActive()) {
            return InteractionResult.PASS;
         } else if (this.bird().level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
         } else {
            ItemStack eaten = stack.copy();
            eaten.setCount(1);
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            this.startBreeding(eaten, player);
            return InteractionResult.SUCCESS;
         }
      }
   }

   public boolean isBreedingFood(ItemStack stack) {
      return !stack.isEmpty() && stack.is(BirdCameraItemTags.BIRD_BREED_FOOD);
   }

   protected void startBreeding(ItemStack eaten, Player player) {
      this.bird().setInLove(player);
   }

   public void setGender(boolean isMale) {
      this.bird().getEntityData().set(AbstractBirdEntity.GENDER, isMale);
   }

   public boolean getGender() {
      return (Boolean)this.bird().getEntityData().get(AbstractBirdEntity.GENDER);
   }

   public void randomizeGender() {
      this.setGender(this.bird().getRandom().nextBoolean());
   }

   public boolean getRandomGender() {
      return this.bird().getRandom().nextBoolean();
   }

   public void setEggCount(int eggCount) {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      this.bird()
         .getEntityData()
         .set(AbstractBirdEntity.EGG_COUNT, Mth.clamp(eggCount, miscDatum.eggCountMin(), miscDatum.eggCountMin() + miscDatum.eggCountVariance()));
   }

   public int getEggCount() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int eggCount = (Integer)this.bird().getEntityData().get(AbstractBirdEntity.EGG_COUNT);
      return Mth.clamp(eggCount, miscDatum.eggCountMin(), miscDatum.eggCountMin() + miscDatum.eggCountVariance());
   }

   public void randomizeEggCount() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      this.setEggCount(miscDatum.eggCountMin() + this.bird().getRandom().nextInt(miscDatum.eggCountVariance()));
   }

   public int inheritEggCount(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
      int parentEggCount = parent.getEggCount();
      int mateEggCount = mate.getEggCount();
      double mean = (double)(parentEggCount + mateEggCount) / 2.0;
      double stdDev = 1.5;
      double value = mean + stdDev * parent.getRandom().nextGaussian();
      int result = (int)Math.round(value);
      BirdMiscDatum misc = parent.getBirdData().misc();
      BirdMiscDatum misc1 = mate.getBirdData().misc();
      int min = Math.min(misc.eggCountMin(), misc1.eggCountMin());
      int max = Math.min(misc.eggCountMin() + misc.eggCountVariance(), misc1.eggCountMin() + misc1.eggCountVariance());
      return Math.clamp((long)result, min, max);
   }
}
