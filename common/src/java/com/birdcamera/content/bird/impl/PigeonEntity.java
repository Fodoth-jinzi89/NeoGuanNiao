package com.birdcamera.content.bird.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.SimpleNeoBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdAnimationController;
import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.registry.BirdCameraBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.animation.RawAnimation;

public class PigeonEntity extends SimpleNeoBirdEntity<PigeonEntity> {
   public PigeonEntity(EntityType<PigeonEntity> entityType, Level level) {
      super(
         entityType,
         level,
         (BirdData)BirdCameraBirdData.PIGEON,
         BirdControllers.<PigeonEntity>builder()
            .birdAnimationController(
               new BirdAnimationController<PigeonEntity>() {
                  @Override
                  public RawAnimation pickFlyAnimation() {
                     Map<String, RawAnimation> animationMap = this.bird().getBirdData().animation().animationMap();
                     if (this.bird().getDeltaMovement().y() > 0.05) {
                        return animationMap.get("fly");
                     } else {
                        ClipContext context = new ClipContext(
                           this.bird().position(), this.bird().position().subtract(0.0, 5.0, 0.0), Block.COLLIDER, Fluid.NONE, this.bird()
                        );
                        BlockHitResult hit = this.bird().level().clip(context);
                        return hit.getType() != Type.MISS ? animationMap.get("fly") : animationMap.get("fly_glide");
                     }
                  }
               }
            )
            .build()
      );
   }

   protected PigeonEntity getSelf() {
      return this;
   }

   public static Builder createAttributes() {
      return SimpleNeoBirdEntity.createAttributes(8.0, 0.22, 0.42, 18.0);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      BlockState below = level.getBlockState(pos.below());
      boolean validGround = below.is(BlockTags.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(BlockTags.SAND) || below.is(Blocks.FARMLAND);
      if (!validGround) {
         return false;
      } else if (!AbstractBirdEntity.hasLocalSpawnCapacity(entityType, level, spawnType, pos, (BirdData)BirdCameraBirdData.PIGEON)) {
         return false;
      } else {
         int score = habitatScore(level, pos);
         return score >= 12 || score >= 7 && random.nextFloat() < 0.55F;
      }
   }

   private static int habitatScore(LevelReader level, BlockPos origin) {
      int score = 0;

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, -2, -7), origin.offset(7, 5, 7))) {
         if (canReadChunk(level, pos)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH) || state.getBlock() instanceof CropBlock) {
               score += 2;
            } else if (state.is(Blocks.PODZOL) || state.is(Blocks.GRASS_BLOCK) || state.getBlock() instanceof ComposterBlock) {
               score += 3;
            } else if (state.is(BlockTags.WALLS) || state.is(BlockTags.LEAVES)) {
               score++;
            } else if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock) {
               score += 2;
            } else if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof BedBlock) {
               score += 4;
            }

            if (score >= 24) {
               return score;
            }
         }
      }

      return score;
   }

   private static boolean canReadChunk(LevelReader level, BlockPos pos) {
      return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
   }

   @Override
   protected List<Goal> buildGoals() {
      return new ArrayList<>(super.buildGoals());
   }
}
