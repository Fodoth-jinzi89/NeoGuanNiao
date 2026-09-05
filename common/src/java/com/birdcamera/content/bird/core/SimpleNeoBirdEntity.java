package com.birdcamera.content.bird.core;

import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.data.BirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class SimpleNeoBirdEntity<T extends SimpleNeoBirdEntity<T>> extends AbstractBirdEntity<T> {
   protected SimpleNeoBirdEntity(EntityType<T> entityType, Level level, BirdData birdData) {
      this(entityType, level, birdData, BirdControllers.<T>builder().build());
   }

   protected SimpleNeoBirdEntity(EntityType<T> entityType, Level level, BirdData birdData, BirdControllers<T> controllers) {
      super(entityType, level, birdData, controllers);
      this.initControllers();
   }

   public static Builder createAttributes(double health, double walkSpeed, double flyingSpeed, double followRange) {
      return TamableAnimal.createMobAttributes()
         .add(Attributes.MAX_HEALTH, health)
         .add(Attributes.MOVEMENT_SPEED, walkSpeed)
         .add(Attributes.FLYING_SPEED, flyingSpeed)
         .add(Attributes.FOLLOW_RANGE, followRange);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType,
      ServerLevelAccessor level,
      MobSpawnType spawnType,
      BlockPos pos,
      RandomSource random,
      BirdData birdData
   ) {
      return AbstractBirdEntity.canSpawn(entityType, level, spawnType, pos, random, birdData);
   }
}
