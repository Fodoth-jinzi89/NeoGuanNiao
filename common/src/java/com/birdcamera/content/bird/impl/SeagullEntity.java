package com.birdcamera.content.bird.impl;

import com.birdcamera.content.bath.BirdBathAttraction;
import com.birdcamera.content.bath.BirdBathBlockEntity;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.SimpleNeoBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdBreedController;
import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdBathUseGoalController;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.registry.BirdCameraBirdData;
import com.birdcamera.registry.BirdCameraItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SeagullEntity extends SimpleNeoBirdEntity<SeagullEntity> {
   public SeagullEntity(EntityType<SeagullEntity> type, Level level) {
      super(
         type,
         level,
         (BirdData)BirdCameraBirdData.SEAGULL,
         BirdControllers.<SeagullEntity>builder().birdEatingController(new BirdEatingController<SeagullEntity>() {
            @Override
            public boolean isEdibleFood(ItemStack stack) {
               return stack.is(BirdCameraItemTags.BIRD_FOOD_FISH) || stack.is(BirdCameraItemTags.SEAGULL_EXTRA_FOOD);
            }
         }).birdBreedController(new BirdBreedController<SeagullEntity>() {
            @Override
            public boolean isBreedingFood(ItemStack stack) {
               return !stack.isEmpty() && stack.is(BirdCameraItemTags.BIRD_BREED_FOOD_FISH);
            }
         }).birdGoalController(BirdGoalController.<SeagullEntity>builder().birdBathUseGoalController(new BirdBathUseGoalController<SeagullEntity>() {
            @Override
            public boolean canUseBathPredicates(BirdBathBlockEntity bath) {
               return BirdBathAttraction.isAttractiveToNightHeron(bath);
            }
         }).build()).build()
      );
   }

   protected SeagullEntity getSelf() {
      return this;
   }

   public static Builder createAttributes() {
      return SimpleNeoBirdEntity.createAttributes(8.0, 0.27, 0.62, 22.0);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, (BirdData)BirdCameraBirdData.SEAGULL);
   }
}
