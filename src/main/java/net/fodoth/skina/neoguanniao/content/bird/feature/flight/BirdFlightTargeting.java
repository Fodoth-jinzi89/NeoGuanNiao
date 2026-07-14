package net.fodoth.skina.neoguanniao.content.bird.feature.flight;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟类飞行目标定位工具
 * 提供飞行目标位置计算、着陆点寻找和安全检查等功能
 */
public final class BirdFlightTargeting {

    private static final double FORWARD_CONE_RADIANS = Math.toRadians(15.0);
    private static final double FORWARD_FALLBACK_RADIANS = 0.62;
    private static final double WIDE_FALLBACK_RADIANS = 1.35;

    private BirdFlightTargeting() {
    }

    /**
     * 生成随机水平方向向量
     */
    @NotNull
    public static Vec3 randomHorizontalDirection(RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
    }

    /**
     * 规范化水平向量，如果向量太小则使用后备向量
     */
    @NotNull
    public static Vec3 normalizeHorizontal(Vec3 vector, Vec3 fallback) {
        Vec3 horizontal = vector.multiply(1.0, 0.0, 1.0);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = fallback.multiply(1.0, 0.0, 1.0);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return horizontal.normalize();
    }

    /**
     * 在水平方向上旋转向量
     */
    @NotNull
    public static Vec3 rotateHorizontal(Vec3 direction, double angle) {
        Vec3 normalized = normalizeHorizontal(direction, new Vec3(1.0, 0.0, 0.0));
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                normalized.x * cos - normalized.z * sin,
                0.0,
                normalized.x * sin + normalized.z * cos
        ).normalize();
    }

    /**
     * 寻找空中目标位置
     */
    @Nullable
    public static Vec3 findAirTarget(PathfinderMob bird, BirdFlightProfile profile, Vec3 preferredDirection, boolean fleeing) {
        Vec3 viewDirection = normalizeHorizontal(bird.getViewVector(1.0F), bird.getLookAngle());
        Vec3 preferred = normalizeHorizontal(preferredDirection, bird.getDeltaMovement());

        Vec3 baseDirection = fleeing
                ? preferred
                : normalizeHorizontal(
                viewDirection.scale(0.78).add(preferred.scale(0.22)),
                viewDirection
        );

        RandomSource random = bird.getRandom();

        for (int attempt = 0; attempt < 24; ++attempt) {
            double turnLimit = fleeing
                    ? (attempt < 14 ? 0.78 : 1.12)
                    : (attempt < 14 ? FORWARD_CONE_RADIANS : (attempt < 20 ? FORWARD_FALLBACK_RADIANS : WIDE_FALLBACK_RADIANS));

            Vec3 direction = rotateHorizontal(baseDirection, randomSigned(random, turnLimit));
            double distance = profile.minAirTargetDistance()
                    + Math.sqrt(random.nextDouble())
                    * (profile.maxAirTargetDistance() - profile.minAirTargetDistance());

            Vec3 horizontalTarget = bird.position().add(direction.scale(distance));
            int blockX = Mth.floor(horizontalTarget.x);
            int blockZ = Mth.floor(horizontalTarget.z);
            Level level = bird.level();

            if (level.hasChunk(blockX >> 4, blockZ >> 4)) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
                if (surfaceY > level.getMinBuildHeight() && surfaceY < level.getMaxBuildHeight() - 6) {
                    double targetY = surfaceY + profile.minCruiseHeight()
                            + random.nextDouble() * Math.max(0.0, profile.maxCruiseHeight() - profile.minCruiseHeight());
                    targetY = Mth.clamp(targetY, bird.getY() - profile.maxVerticalStep(), bird.getY() + profile.maxVerticalStep());
                    targetY = Mth.clamp(targetY, level.getMinBuildHeight() + 3.0, level.getMaxBuildHeight() - 3.0);

                    BlockPos airPos = BlockPos.containing(horizontalTarget.x, targetY, horizontalTarget.z);
                    if (isOpenAir(bird, airPos)) {
                        return new Vec3(blockX + 0.5, targetY, blockZ + 0.5);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 在指定方向寻找着陆点
     */
    @Nullable
    public static Vec3 findLandingInDirection(
            PathfinderMob bird,
            Vec3 direction,
            int minRadius,
            int maxRadius,
            int horizontalRange,
            int verticalRange
    ) {
        Vec3 horizontal = normalizeHorizontal(direction, bird.getLookAngle());
        RandomSource random = bird.getRandom();

        for (int attempt = 0; attempt < 18; ++attempt) {
            double radius = minRadius + random.nextDouble() * Math.max(1, maxRadius - minRadius);
            Vec3 rotated = rotateHorizontal(horizontal, randomSigned(random, 0.85));
            BlockPos center = BlockPos.containing(
                    bird.position().add(rotated.scale(radius)).add(0, 3.5, 0)
            );
            Vec3 landing = findDryLandingTargetNear(bird, center, horizontalRange, verticalRange);
            if (landing != null) {
                return landing;
            }
        }

        return null;
    }

    /**
     * 寻找最近的干燥着陆点
     */
    @Nullable
    public static Vec3 findNearestDryLandingTarget(PathfinderMob bird, int radius, int verticalRange) {
        BlockPos origin = bird.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int r = 2; r <= radius; ++r) {
            for (int xOffset = -r; xOffset <= r; ++xOffset) {
                for (int zOffset = -r; zOffset <= r; ++zOffset) {
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

    /**
     * 在指定位置附近寻找干燥着陆点
     */
    @Nullable
    public static Vec3 findDryLandingTargetNear(PathfinderMob bird, BlockPos center, int horizontalRange, int verticalRange) {
        Vec3 direct = findDryLandingTarget(bird, center, verticalRange);
        if (direct != null) {
            return direct;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int radius = 1; radius <= horizontalRange; ++radius) {
            for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
                for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
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

    /**
     * 在指定位置寻找干燥着陆点
     */
    @Nullable
    public static Vec3 findDryLandingTarget(PathfinderMob bird, BlockPos center, int verticalRange) {
        BlockPos landing = findDryLandingSurface(bird, center, verticalRange);
        return landing == null ? null : Vec3.atBottomCenterOf(landing).add(0, 0.05, 0);
    }

    /**
     * 寻找干燥着陆表面
     */
    @Nullable
    public static BlockPos findDryLandingSurface(PathfinderMob bird, BlockPos center, int verticalRange) {
        Level level = bird.level();
        if (!level.hasChunk(center.getX() >> 4, center.getZ() >> 4)) {
            return null;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (isSafeDryLanding(bird, mutable)) {
                return mutable.immutable();
            }
        }

        return null;
    }

    /**
     * 检查位置是否为开阔空域
     */
    public static boolean isOpenAir(Entity entity, BlockPos pos) {
        Level level = entity.level();
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }
        if (pos.getY() <= level.getMinBuildHeight() + 1 || pos.getY() >= level.getMaxBuildHeight() - 1) {
            return false;
        }

        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        if (feet.getCollisionShape(level, pos).isEmpty() && head.getCollisionShape(level, pos.above()).isEmpty()) {
            AABB box = entity.getBoundingBox()
                    .move(Vec3.atBottomCenterOf(pos).subtract(entity.position()))
                    .inflate(-0.04, -0.02, -0.04);

            return level.noCollision(entity, box)
                    && !level.getFluidState(pos).is(FluidTags.WATER)
                    && !level.getFluidState(pos).is(FluidTags.LAVA)
                    && !level.getFluidState(pos.above()).is(FluidTags.WATER)
                    && !level.getFluidState(pos.above()).is(FluidTags.LAVA);
        }
        return false;
    }

    /**
     * 检查是否为安全的干燥着陆点
     */
    public static boolean isSafeDryLanding(PathfinderMob bird, BlockPos pos) {
        Level level = bird.level();
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        if (!feet.getCollisionShape(level, pos).isEmpty() || !head.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }

        // 检查流体
        if (level.getFluidState(pos).is(FluidTags.WATER)
                || level.getFluidState(pos).is(FluidTags.LAVA)
                || level.getFluidState(pos.below()).is(FluidTags.WATER)
                || level.getFluidState(pos.below()).is(FluidTags.LAVA)) {
            return false;
        }

        // 检查下方方块
        if (below.isAir() || below.is(Blocks.WATER) || below.is(Blocks.LAVA)) {
            return false;
        }

        // 检查是否为可站立表面
        return below.isFaceSturdy(level, pos.below(), Direction.UP)
                || below.is(NeoGuanNiaoBlockTags.BIRD_PERCHES)
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
                || below.getBlock() instanceof FenceGateBlock;
    }

    /**
     * 生成带符号的随机值
     */
    private static double randomSigned(RandomSource random, double value) {
        return (random.nextDouble() * 2.0 - 1.0) * value;
    }
}