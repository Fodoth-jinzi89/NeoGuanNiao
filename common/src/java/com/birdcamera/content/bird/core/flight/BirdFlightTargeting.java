package com.birdcamera.content.bird.core.flight;

import com.birdcamera.content.bird.core.data.datum.BirdFlightProfile;
import com.birdcamera.registry.BirdCameraBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BirdFlightTargeting {
   private static final double FORWARD_CONE_RADIANS = Math.toRadians(15.0);
   private static final double FORWARD_FALLBACK_RADIANS = 0.62;
   private static final double WIDE_FALLBACK_RADIANS = 1.35;

   private BirdFlightTargeting() {
   }

   @NotNull
   public static Vec3 randomHorizontalDirection(RandomSource random) {
      double angle = random.nextDouble() * Math.PI * 2.0;
      return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
   }

   @NotNull
   public static Vec3 normalizeHorizontal(Vec3 vector, Vec3 fallback) {
      Vec3 horizontal = vector.multiply(1.0, 0.0, 1.0);
      if (horizontal.lengthSqr() <= 1.0E-4) {
         horizontal = fallback.multiply(1.0, 0.0, 1.0);
      }

      return horizontal.lengthSqr() <= 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : horizontal.normalize();
   }

   @NotNull
   public static Vec3 rotateHorizontal(Vec3 direction, double angle) {
      Vec3 normalized = normalizeHorizontal(direction, new Vec3(1.0, 0.0, 0.0));
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      return new Vec3(normalized.x * cos - normalized.z * sin, 0.0, normalized.x * sin + normalized.z * cos).normalize();
   }

   @Nullable
   public static Vec3 findAirTarget(PathfinderMob bird, BirdFlightProfile profile, Vec3 preferredDirection, boolean fleeing) {
      Vec3 viewDirection = normalizeHorizontal(bird.getViewVector(1.0F), bird.getLookAngle());
      Vec3 preferred = normalizeHorizontal(preferredDirection, bird.getDeltaMovement());
      Vec3 baseDirection = fleeing ? preferred : normalizeHorizontal(viewDirection.scale(0.78).add(preferred.scale(0.22)), viewDirection);
      RandomSource random = bird.getRandom();

      for (int attempt = 0; attempt < 24; attempt++) {
         double turnLimit = fleeing ? (attempt < 14 ? 0.78 : 1.12) : (attempt < 14 ? FORWARD_CONE_RADIANS : (attempt < 20 ? 0.62 : 1.35));
         Vec3 direction = rotateHorizontal(baseDirection, randomSigned(random, turnLimit));
         double distance = profile.minAirTargetDistance() + Math.sqrt(random.nextDouble()) * (profile.maxAirTargetDistance() - profile.minAirTargetDistance());
         Vec3 horizontalTarget = bird.position().add(direction.scale(distance));
         int blockX = Mth.floor(horizontalTarget.x);
         int blockZ = Mth.floor(horizontalTarget.z);
         Level level = bird.level();
         if (level.hasChunk(blockX >> 4, blockZ >> 4)) {
            int surfaceY = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
            if (surfaceY > level.getMinBuildHeight() && surfaceY < level.getMaxBuildHeight() - 6) {
               double targetY = (double)surfaceY
                  + profile.minCruiseHeight()
                  + random.nextDouble() * Math.max(0.0, profile.maxCruiseHeight() - profile.minCruiseHeight());
               targetY = Mth.clamp(targetY, bird.getY() - profile.maxVerticalStep(), bird.getY() + profile.maxVerticalStep());
               targetY = Mth.clamp(targetY, (double)level.getMinBuildHeight() + 3.0, (double)level.getMaxBuildHeight() - 3.0);
               BlockPos airPos = BlockPos.containing(horizontalTarget.x, targetY, horizontalTarget.z);
               if (isOpenAir(bird, airPos)) {
                  return new Vec3((double)blockX + 0.5, targetY, (double)blockZ + 0.5);
               }
            }
         }
      }

      return null;
   }

   @Nullable
   public static Vec3 findLandingInDirection(PathfinderMob bird, Vec3 direction, int minRadius, int maxRadius, int horizontalRange, int verticalRange) {
      Vec3 horizontal = normalizeHorizontal(direction, bird.getLookAngle());
      RandomSource random = bird.getRandom();

      for (int attempt = 0; attempt < 18; attempt++) {
         double radius = (double)minRadius + random.nextDouble() * (double)Math.max(1, maxRadius - minRadius);
         Vec3 rotated = rotateHorizontal(horizontal, randomSigned(random, 0.85));
         BlockPos center = BlockPos.containing(bird.position().add(rotated.scale(radius)).add(0.0, 3.5, 0.0));
         Vec3 landing = findDryLandingTargetNear(bird, center, horizontalRange, verticalRange);
         if (landing != null) {
            return landing;
         }
      }

      return null;
   }

   @Nullable
   public static Vec3 findNearestDryLandingTarget(PathfinderMob bird, int radius, int verticalRange) {
      BlockPos origin = bird.blockPosition();
      MutableBlockPos mutable = new MutableBlockPos();

      for (int r = 2; r <= radius; r++) {
         for (int xOffset = -r; xOffset <= r; xOffset++) {
            for (int zOffset = -r; zOffset <= r; zOffset++) {
               if (Math.abs(xOffset) == r || Math.abs(zOffset) == r) {
                  mutable.set(origin.getX() + xOffset, origin.getY(), origin.getZ() + zOffset);
                  Vec3 landing = findDryLandingTarget(bird, mutable, verticalRange);
                  if (landing != null) {
                     return landing;
                  }
               }
            }
         }
      }

      return null;
   }

   @Nullable
   public static Vec3 findDryLandingTargetNear(PathfinderMob bird, BlockPos center, int horizontalRange, int verticalRange) {
      Vec3 direct = findDryLandingTarget(bird, center, verticalRange);
      if (direct != null) {
         return direct;
      } else {
         MutableBlockPos mutable = new MutableBlockPos();

         for (int radius = 1; radius <= horizontalRange; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
               for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                  if (Math.abs(xOffset) == radius || Math.abs(zOffset) == radius) {
                     mutable.set(center.getX() + xOffset, center.getY(), center.getZ() + zOffset);
                     Vec3 landing = findDryLandingTarget(bird, mutable, verticalRange);
                     if (landing != null) {
                        return landing;
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public static Vec3 findDryLandingTarget(PathfinderMob bird, BlockPos center, int verticalRange) {
      BlockPos landing = findDryLandingSurface(bird, center, verticalRange);
      return landing == null ? null : Vec3.atBottomCenterOf(landing).add(0.0, 0.05, 0.0);
   }

   @Nullable
   public static BlockPos findDryLandingSurface(PathfinderMob bird, BlockPos center, int verticalRange) {
      Level level = bird.level();
      if (!level.hasChunk(center.getX() >> 4, center.getZ() >> 4)) {
         return null;
      } else {
         MutableBlockPos mutable = new MutableBlockPos();

         for (int yOffset = verticalRange; yOffset >= -verticalRange; yOffset--) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (isSafeDryLanding(bird, mutable)) {
               return mutable.immutable();
            }
         }

         return null;
      }
   }

   public static boolean isOpenAir(Entity entity, BlockPos pos) {
      Level level = entity.level();
      if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
         return false;
      } else if (pos.getY() > level.getMinBuildHeight() + 1 && pos.getY() < level.getMaxBuildHeight() - 1) {
         BlockState feet = level.getBlockState(pos);
         BlockState head = level.getBlockState(pos.above());
         if (feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty()) {
            AABB box = entity.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(entity.position())).inflate(-0.04, -0.02, -0.04);
            return level.noCollision(entity, box)
               && !level.getFluidState(pos).is(FluidTags.WATER)
               && !level.getFluidState(pos).is(FluidTags.LAVA)
               && !level.getFluidState(pos.above()).is(FluidTags.WATER)
               && !level.getFluidState(pos.above()).is(FluidTags.LAVA);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isSafeDryLanding(PathfinderMob bird, BlockPos pos) {
      Level level = bird.level();
      if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
         return false;
      } else {
         BlockState below = level.getBlockState(pos.below());
         BlockState feet = level.getBlockState(pos);
         BlockState head = level.getBlockState(pos.above());
         if (feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty()) {
            if (level.getFluidState(pos).is(FluidTags.WATER)
               || level.getFluidState(pos).is(FluidTags.LAVA)
               || level.getFluidState(pos.below()).is(FluidTags.WATER)
               || level.getFluidState(pos.below()).is(FluidTags.LAVA)) {
               return false;
            } else {
               return !below.isAir() && !below.is(Blocks.WATER) && !below.is(Blocks.LAVA)
                  ? below.isFaceSturdy(level, pos.below(), Direction.UP)
                     || below.is(BirdCameraBlockTags.BIRD_PERCHES)
                     || below.is(BlockTags.FENCES)
                     || below.is(BlockTags.WALLS)
                     || below.is(BlockTags.LEAVES)
                     || below.is(BlockTags.DIRT)
                     || below.is(BlockTags.SAND)
                     || below.is(Blocks.FARMLAND)
                     || below.is(Blocks.GRASS_BLOCK)
                     || below.is(Blocks.PODZOL)
                     || below.is(Blocks.MYCELIUM)
                     || below.getBlock() instanceof FenceBlock
                     || below.getBlock() instanceof FenceGateBlock
                  : false;
            }
         } else {
            return false;
         }
      }
   }

   private static double randomSigned(RandomSource random, double value) {
      return (random.nextDouble() * 2.0 - 1.0) * value;
   }
}
