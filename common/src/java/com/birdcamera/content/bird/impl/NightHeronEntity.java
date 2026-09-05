package com.birdcamera.content.bird.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.birdcamera.content.bath.BirdBathAttraction;
import com.birdcamera.content.bath.BirdBathBlockEntity;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.SimpleNeoBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdAnimationController;
import com.birdcamera.content.bird.core.controller.BirdBreedController;
import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdBathUseGoalController;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.registry.BirdCameraBirdData;
import com.birdcamera.registry.BirdCameraBlockTags;
import com.birdcamera.registry.BirdCameraItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.RawAnimation;

public class NightHeronEntity extends SimpleNeoBirdEntity<NightHeronEntity> {
   private static final EntityDataAccessor<ItemStack> HELD_FISH = SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.ITEM_STACK);
   static final Ingredient TEMPT_ITEMS = Ingredient.of(new ItemLike[]{Items.COD, Items.SALMON, Items.COOKED_COD, Items.COOKED_SALMON});

   public NightHeronEntity(EntityType<NightHeronEntity> entityType, Level level) {
      super(
         entityType,
         level,
         (BirdData)BirdCameraBirdData.NIGHT_HERON,
         BirdControllers.<NightHeronEntity>builder()
            .birdEatingController(new BirdEatingController<NightHeronEntity>() {
               @Override
               public boolean isEdibleFood(ItemStack stack) {
                  return stack.is(BirdCameraItemTags.BIRD_FOOD_FISH);
               }
            })
            .birdBreedController(new BirdBreedController<NightHeronEntity>() {
               @Override
               public boolean isBreedingFood(ItemStack stack) {
                  return !stack.isEmpty() && stack.is(BirdCameraItemTags.BIRD_BREED_FOOD_FISH);
               }
            })
            .birdAnimationController(
               new BirdAnimationController<NightHeronEntity>() {
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
            .birdGoalController(BirdGoalController.<NightHeronEntity>builder().birdBathUseGoalController(new BirdBathUseGoalController<NightHeronEntity>() {
               @Override
               public boolean canUseBathPredicates(BirdBathBlockEntity bath) {
                  return BirdBathAttraction.isAttractiveToNightHeron(bath);
               }
            }).build())
            .build()
      );
   }

   protected NightHeronEntity getSelf() {
      return this;
   }

   @Override
   protected void initPathfindingMalus() {
      this.setPathfindingMalus(PathType.LEAVES, 0.0F);
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
   }

   @Override
   protected void defineSynchedData(@NotNull Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HELD_FISH, ItemStack.EMPTY);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType,
      ServerLevelAccessor level,
      MobSpawnType spawnType,
      BlockPos pos,
      RandomSource random,
      BirdData birdData
   ) {
      BlockState below = level.getBlockState(pos.below());
      boolean validGround = below.is(BlockTags.DIRT)
         || below.is(BlockTags.SAND)
         || below.is(Blocks.GRASS_BLOCK)
         || below.is(Blocks.DIRT_PATH)
         || below.is(Blocks.FARMLAND)
         || below.is(BirdCameraBlockTags.BIRD_PERCHES);
      if (!validGround) {
         return false;
      } else {
         return AbstractBirdEntity.hasLocalSpawnCapacity(entityType, level, spawnType, pos, birdData)
            && isNearWaterForWorldgen(level, pos, 8);
      }
   }

   private static boolean isNearWaterForWorldgen(LevelReader level, BlockPos pos, int radius) {
      return level instanceof WorldGenRegion ? isNearWaterInSpawnChunk(level, pos, radius) : isNearWater(level, pos, radius);
   }

   private static boolean isWaterEdgeForWorldgen(LevelReader level, BlockPos pos) {
      return level instanceof WorldGenRegion ? isWaterEdgeInSpawnChunk(level, pos) : isWaterEdge(level, pos);
   }

   private static boolean isWaterEdgeInSpawnChunk(LevelReader level, BlockPos pos) {
      int spawnChunkX = SectionPos.blockToSectionCoord(pos.getX());
      int spawnChunkZ = SectionPos.blockToSectionCoord(pos.getZ());
      if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
         return true;
      } else {
         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (isInChunk(adjacentPos, spawnChunkX, spawnChunkZ)
               && (level.getFluidState(adjacentPos).is(FluidTags.WATER) || level.getFluidState(adjacentPos.below()).is(FluidTags.WATER))) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isWaterEdge(LevelReader level, BlockPos pos) {
      if (!canReadChunk(level, pos)) {
         return false;
      } else if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
         return true;
      } else {
         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (canReadChunk(level, adjacentPos)
               && (level.getFluidState(adjacentPos).is(FluidTags.WATER) || level.getFluidState(adjacentPos.below()).is(FluidTags.WATER))) {
               return true;
            }
         }

         return false;
      }
   }

   static boolean isNearWater(LevelReader level, BlockPos pos, int radius) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int xOffset = -radius; xOffset <= radius; xOffset++) {
         for (int zOffset = -radius; zOffset <= radius; zOffset++) {
            if (xOffset * xOffset + zOffset * zOffset <= radius * radius) {
               for (int yOffset = -1; yOffset <= 1; yOffset++) {
                  mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                  if (canReadChunk(level, mutablePos) && level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isNearWaterInSpawnChunk(LevelReader level, BlockPos pos, int radius) {
      MutableBlockPos mutablePos = new MutableBlockPos();
      int spawnChunkX = SectionPos.blockToSectionCoord(pos.getX());
      int spawnChunkZ = SectionPos.blockToSectionCoord(pos.getZ());

      for (int xOffset = -radius; xOffset <= radius; xOffset++) {
         for (int zOffset = -radius; zOffset <= radius; zOffset++) {
            int x = pos.getX() + xOffset;
            int z = pos.getZ() + zOffset;
            if (xOffset * xOffset + zOffset * zOffset <= radius * radius && isInChunk(x, z, spawnChunkX, spawnChunkZ)) {
               for (int yOffset = -1; yOffset <= 1; yOffset++) {
                  mutablePos.set(x, pos.getY() + yOffset, z);
                  if (level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
      return isInChunk(pos.getX(), pos.getZ(), chunkX, chunkZ);
   }

   private static boolean isInChunk(int x, int z, int chunkX, int chunkZ) {
      return SectionPos.blockToSectionCoord(x) == chunkX && SectionPos.blockToSectionCoord(z) == chunkZ;
   }

   public static boolean canReadChunk(LevelReader level, BlockPos pos) {
      return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return SimpleNeoBirdEntity.createAttributes(16.0, 0.32, 0.65, 32.0).add(Attributes.ATTACK_DAMAGE, 2.0);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return AbstractBirdEntity.canSpawn(entityType, level, spawnType, pos, random, (BirdData)BirdCameraBirdData.NIGHT_HERON);
   }

   @Override
   protected List<Goal> buildGoals() {
      return new ArrayList<>(super.buildGoals());
   }

   public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
      float score = super.getWalkTargetValue(pos, level);
      BlockState below = level.getBlockState(pos.below());
      if (isNearWaterForWorldgen(level, pos, 4)) {
         score += 8.0F;
      }

      if (isWaterEdgeForWorldgen(level, pos)) {
         score += 6.0F;
      }

      if (below.is(Blocks.WATER) || below.is(Blocks.SEAGRASS) || below.is(Blocks.GRASS_BLOCK) || below.is(BlockTags.SAND)) {
         score += 2.0F;
      }

      return score;
   }

   public ItemStack getHeldFishForRendering() {
      return (ItemStack)this.getEntityData().get(HELD_FISH);
   }

   public boolean hasHeldFishForRendering() {
      return !this.getHeldFishForRendering().isEmpty();
   }
}
