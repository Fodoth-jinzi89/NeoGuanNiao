package com.birdcamera.content.bird.impl;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.SimpleNeoBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.registry.BirdCameraBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class LongTailedTitEntity extends SimpleNeoBirdEntity<LongTailedTitEntity> {
   public LongTailedTitEntity(EntityType<LongTailedTitEntity> type, Level level) {
      super(type, level, (BirdData)BirdCameraBirdData.LONG_TAILED_TIT);
   }

   protected LongTailedTitEntity getSelf() {
      return this;
   }

   public static Builder createAttributes() {
      return SimpleNeoBirdEntity.createAttributes(6.0, 0.25, 0.34, 18.0);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, (BirdData)BirdCameraBirdData.LONG_TAILED_TIT);
   }
}
