package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.content.bird.core.skin.BirdSkinUtils;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import com.birdcamera.registry.BirdCameraItemTags;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BirdFoodBagController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!this.isFilledFoodBag(stack)) {
         return InteractionResult.PASS;
      } else if (this.bird().getEatingController().isEating()) {
         return InteractionResult.PASS;
      } else if (this.isBusyWithSpecialActivities()) {
         return InteractionResult.PASS;
      } else if (this.isRestingOrFlying()) {
         return InteractionResult.PASS;
      } else {
         return this.bird().level().isClientSide ? InteractionResult.sidedSuccess(true) : this.processFoodBag(stack, player);
      }
   }

   protected boolean isFilledFoodBag(ItemStack stack) {
      return !stack.isEmpty() && stack.is(BirdCameraItemTags.FILLED_FOOD_BAG);
   }

   protected boolean isBusyWithSpecialActivities() {
      if (this.bird() instanceof BudgerigarEntity b && b.isBusyWithMusicOrSleep()) {
         return true;
      }

      return false;
   }

   protected boolean isRestingOrFlying() {
      return this.bird().getRoutineController().isSleepingOrRoosting() || this.bird().getFlyingController().isBirdFlightActive();
   }

   protected InteractionResult processFoodBag(ItemStack stack, Player player) {
      BirdFoodBagController.FoodBagStrategy strategy = BirdFoodBagController.FoodBagStrategy.fromItem(stack);
      return strategy == null ? InteractionResult.PASS : strategy.execute(this, this.bird(), stack, player);
   }

   private boolean checkBabyOrFail(Player player) {
      if (this.bird().isBaby()) {
         return true;
      } else {
         player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.adult_failed"), true);
         return false;
      }
   }

   private boolean checkAdultOrFail(Player player) {
      if (!this.bird().isBaby()) {
         return true;
      } else {
         player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.baby_failed"), true);
         return false;
      }
   }

   private void spawnParticles(ParticleOptions particle, int count, double speed) {
      if (!this.bird().level().isClientSide) {
         ServerLevel level = (ServerLevel)this.bird().level();
         level.sendParticles(
            particle, this.bird().getX(), this.bird().getY() + (double)this.bird().getBbHeight() * 0.5, this.bird().getZ(), count, 0.3, 0.3, 0.3, speed
         );
      }
   }

   private void consumeItem(ItemStack stack, Player player) {
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
   }

   private boolean tryApplySkin(Player player, BirdSkin target, String failKey) {
      if (target == null) {
         player.displayClientMessage(Component.translatable(failKey), true);
         return false;
      } else {
         this.bird().getSkinController().setSkinVariant(target.id());
         return true;
      }
   }

   private int getMaxValue(int min, int variance) {
      return min + variance;
   }

   private int getIntervalLowerBound(int middle, int variance) {
      return middle - variance / 2;
   }

   private int getIntervalUpperBound(int middle, int variance) {
      return middle + variance / 2;
   }

   private static enum FoodBagStrategy {
      GROWTH(BirdCameraItems.GREEN_FOOD_BAG_GROWTH, (ctrl, bird, stack, player) -> {
         if (!ctrl.checkBabyOrFail(player)) {
            return InteractionResult.FAIL;
         } else {
            bird.setAge(0);
            ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0.0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      REJUVENATE(BirdCameraItems.GREEN_FOOD_BAG_REJUVENATE, (ctrl, bird, stack, player) -> {
         if (!ctrl.checkAdultOrFail(player)) {
            return InteractionResult.FAIL;
         } else {
            bird.setAge(-24000);
            ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0.0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      STOP_GROWTH(BirdCameraItems.GREEN_FOOD_BAG_STOP, (ctrl, bird, stack, player) -> {
         if (!ctrl.checkBabyOrFail(player)) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.stop_failed"), true);
            return InteractionResult.FAIL;
         } else {
            boolean stopped = bird.isGrowthStopped();
            bird.setGrowthStopped(!stopped);
            if (stopped) {
               ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0.0);
               player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.growth_resumed"), true);
            } else {
               ctrl.spawnParticles(ParticleTypes.SMOKE, 8, 0.01);
               player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.growth_stopped"), true);
            }

            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      TRANSMUTE(BirdCameraItems.GREEN_FOOD_BAG_TRANSMUTE, (ctrl, bird, stack, player) -> {
         BirdSkin target = BirdSkinUtils.findOppositeGenderSkin(bird, bird.getSkin());
         if (!ctrl.tryApplySkin(player, target, "message.birdcamera.bird_food_bag.transmute_failed")) {
            return InteractionResult.FAIL;
         } else {
            bird.setMale(!bird.isMale());
            ctrl.spawnParticles(ParticleTypes.DRAGON_BREATH, 10, 0.0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      UPGRADE(BirdCameraItems.GOLDEN_FOOD_BAG_UPGRADE, (ctrl, bird, stack, player) -> {
         BirdSkin target = BirdSkinUtils.findUpgradeSkinBeforeAncient(bird, bird.getSkin());
         if (!ctrl.tryApplySkin(player, target, "message.birdcamera.golden_food_bag.upgrade_failed")) {
            return InteractionResult.FAIL;
         } else {
            ctrl.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 15, 0.0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      DOWNGRADE(BirdCameraItems.GOLDEN_FOOD_BAG_DOWNGRADE, (ctrl, bird, stack, player) -> {
         BirdSkin target = BirdSkinUtils.findDowngradeSkinBeforeAncient(bird, bird.getSkin());
         if (!ctrl.tryApplySkin(player, target, "message.birdcamera.golden_food_bag.downgrade_failed")) {
            return InteractionResult.FAIL;
         } else {
            ctrl.spawnParticles(ParticleTypes.FALLING_NECTAR, 15, 0.01);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      UNIQUE(BirdCameraItems.GOLDEN_FOOD_BAG_UNIQUE, (ctrl, bird, stack, player) -> {
         BirdSkin target = BirdSkinUtils.findRandomUniqueSkin(bird);
         if (!ctrl.tryApplySkin(player, target, "message.birdcamera.golden_food_bag.unique_failed")) {
            return InteractionResult.FAIL;
         } else {
            ctrl.spawnParticles(ParticleTypes.CHERRY_LEAVES, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      FEATHER_ADD(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_ADD, (ctrl, bird, stack, player) -> {
         int max = ctrl.getMaxValue(bird.getBirdData().misc().featherCountMin(), bird.getBirdData().misc().featherCountVariance());
         if (bird.getFeatherCount() >= max) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.feather_added_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setFeatherCount(bird.getFeatherCount() + 1);
            ctrl.spawnParticles(ParticleTypes.ENCHANTED_HIT, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      FEATHER_MINUS(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_MINUS, (ctrl, bird, stack, player) -> {
         int min = bird.getBirdData().misc().featherCountMin();
         if (bird.getFeatherCount() <= min) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.feather_minus_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setFeatherCount(bird.getFeatherCount() - 1);
            ctrl.spawnParticles(ParticleTypes.CRIT, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      FEATHER_FAST(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_FAST, (ctrl, bird, stack, player) -> {
         int fasten = bird.getFeatherInterval() - 1000;
         int lowerBound = ctrl.getIntervalLowerBound(bird.getBirdData().misc().featherIntervalMiddle(), bird.getBirdData().misc().featherIntervalVariance());
         if (fasten < lowerBound) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.feather_interval_fasten_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setFeatherInterval(fasten);
            ctrl.spawnParticles(ParticleTypes.FLAME, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      FEATHER_SLOW(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_SLOW, (ctrl, bird, stack, player) -> {
         int slow = bird.getFeatherInterval() + 1000;
         int upperBound = ctrl.getIntervalUpperBound(bird.getBirdData().misc().featherIntervalMiddle(), bird.getBirdData().misc().featherIntervalVariance());
         if (slow > upperBound) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.feather_interval_slow_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setFeatherInterval(slow);
            ctrl.spawnParticles(ParticleTypes.FALLING_WATER, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      EGG_ADD(BirdCameraItems.GOLDEN_FOOD_BAG_EGG_ADD, (ctrl, bird, stack, player) -> {
         int max = ctrl.getMaxValue(bird.getBirdData().misc().eggCountMin(), bird.getBirdData().misc().eggCountVariance());
         if (bird.getEggCount() >= max) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.egg_added_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setEggCount(bird.getEggCount() + 1);
            ctrl.spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      EGG_MINUS(BirdCameraItems.GOLDEN_FOOD_BAG_EGG_MINUS, (ctrl, bird, stack, player) -> {
         int min = bird.getBirdData().misc().eggCountMin();
         if (bird.getEggCount() <= min) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.egg_minus_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setEggCount(bird.getEggCount() - 1);
            ctrl.spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      SIZE_UP(BirdCameraItems.GREEN_FOOD_BAG_SIZE_UP, (ctrl, bird, stack, player) -> {
         float sizeUp = bird.getIndividualModelScale() + 0.05F;
         float upperBound = bird.getBirdData().model().modelScaleProfile().maxIndividualScale();
         if (sizeUp > upperBound) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.size_up_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setIndividualModelScale(sizeUp);
            ctrl.spawnParticles(ParticleTypes.ITEM_SLIME, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      }),
      SIZE_DOWN(BirdCameraItems.GREEN_FOOD_BAG_SIZE_DOWN, (ctrl, bird, stack, player) -> {
         float sizeDown = bird.getIndividualModelScale() - 0.05F;
         float lowerBound = bird.getBirdData().model().modelScaleProfile().minIndividualScale();
         if (sizeDown < lowerBound) {
            player.displayClientMessage(Component.translatable("message.birdcamera.bird_food_bag.size_down_failed"), true);
            return InteractionResult.FAIL;
         } else {
            bird.setIndividualModelScale(sizeDown);
            ctrl.spawnParticles(ParticleTypes.ITEM_SNOWBALL, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
         }
      });

      private final Item item;
      private final BirdFoodBagController.FoodBagStrategy.FoodBagAction action;

      private FoodBagStrategy(Item item, BirdFoodBagController.FoodBagStrategy.FoodBagAction action) {
         this.item = item;
         this.action = action;
      }

      static BirdFoodBagController.FoodBagStrategy fromItem(ItemStack stack) {
         for (BirdFoodBagController.FoodBagStrategy strategy : values()) {
            if (stack.is(strategy.item)) {
               return strategy;
            }
         }

         return null;
      }

      InteractionResult execute(BirdFoodBagController<?> ctrl, AbstractBirdEntity<?> bird, ItemStack stack, Player player) {
         return this.action.apply(ctrl, bird, stack, player);
      }

      @FunctionalInterface
      private interface FoodBagAction {
         InteractionResult apply(BirdFoodBagController<?> var1, AbstractBirdEntity<?> var2, ItemStack var3, Player var4);
      }
   }
}
