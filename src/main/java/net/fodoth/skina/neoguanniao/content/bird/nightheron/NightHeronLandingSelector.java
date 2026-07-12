package net.fodoth.skina.neoguanniao.content.bird.nightheron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 夜鹭着陆点选择器
 * 为夜鹭选择最佳的着陆位置，包括逃离、转场和栖息着陆
 */
public final class NightHeronLandingSelector {

    private NightHeronLandingSelector() {
    }

    // ============ 公共方法 ============

    /**
     * 寻找逃离着陆点
     */
    public static BlockPos findEscapeLanding(NightHeronEntity nightHeron, Vec3 threatPosition, int minRadius, int maxRadius) {
        return findBestLanding(nightHeron, threatPosition, minRadius, maxRadius, false);
    }

    /**
     * 寻找转场着陆点
     */
    public static BlockPos findTransitLanding(NightHeronEntity nightHeron, int minRadius, int maxRadius) {
        return findBestLanding(nightHeron, null, minRadius, maxRadius, !nightHeron.isActiveTime());
    }

    /**
     * 寻找栖息着陆点
     */
    public static BlockPos findRoostLanding(NightHeronEntity nightHeron, int minRadius, int maxRadius) {
        return findBestLanding(nightHeron, null, minRadius, maxRadius, true);
    }

    /**
     * 计算从夜鹭到目标的方向
     */
    public static Vec3 directionTo(BlockPos target, NightHeronEntity nightHeron) {
        Vec3 direction = Vec3.atCenterOf(target).subtract(nightHeron.position());
        Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            return nightHeron.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        }
        return horizontal.normalize();
    }

    // ============ 核心着陆选择 ============

    /**
     * 寻找最佳着陆点
     */
    private static BlockPos findBestLanding(NightHeronEntity nightHeron, Vec3 threatPosition, int minRadius, int maxRadius, boolean preferRoost) {
        Level level = nightHeron.level();
        BlockPos origin = nightHeron.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int horizontalStep = maxRadius > 64 ? 2 : 1;

        for (int xOffset = -maxRadius; xOffset <= maxRadius; xOffset += horizontalStep) {
            for (int zOffset = -maxRadius; zOffset <= maxRadius; zOffset += horizontalStep) {
                int horizontalDistanceSqr = xOffset * xOffset + zOffset * zOffset;
                if (horizontalDistanceSqr >= minRadius * minRadius && horizontalDistanceSqr <= maxRadius * maxRadius) {
                    mutablePos.set(origin.getX() + xOffset, origin.getY(), origin.getZ() + zOffset);

                    if (level.hasChunk(SectionPos.blockToSectionCoord(mutablePos.getX()), SectionPos.blockToSectionCoord(mutablePos.getZ()))) {
                        BlockPos landingPos = findSurface(level, mutablePos, 12);
                        if (landingPos != null && isSafeLanding(level, landingPos)) {
                            double score = scoreLanding(nightHeron, landingPos, threatPosition, preferRoost);
                            if (score > bestScore) {
                                bestScore = score;
                                bestPos = landingPos.immutable();
                            }
                        }
                    }
                }
            }
        }

        return bestPos;
    }

    /**
     * 在指定位置附近寻找着陆表面
     */
    @SuppressWarnings("SameParameterValue")
    private static BlockPos findSurface(Level level, BlockPos center, int verticalRange) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutablePos.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (isSafeLanding(level, mutablePos)) {
                return mutablePos.immutable();
            }
        }
        return null;
    }

    // ============ 安全检查 ============

    /**
     * 检查是否为安全的着陆点
     */
    private static boolean isSafeLanding(Level level, BlockPos pos) {
        if (!NightHeronEntity.canReadChunk(level, pos)) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        // 检查碰撞箱
        if (!feet.getCollisionShape(level, pos).isEmpty() || !head.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }

        // 检查流体
        if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }

        // 检查下方方块
        if (below.is(Blocks.WATER) || below.is(Blocks.LAVA)) {
            return false;
        }

        return below.isFaceSturdy(level, pos.below(), Direction.UP)
                || below.is(BlockTags.WALLS)
                || below.is(BlockTags.LEAVES);
    }

    // ============ 评分系统 ============

    /**
     * 为着陆点评分
     */
    private static double scoreLanding(NightHeronEntity nightHeron, BlockPos pos, Vec3 threatPosition, boolean preferRoost) {
        Level level = nightHeron.level();
        BlockState below = level.getBlockState(pos.below());
        double score = nightHeron.isNearWater(pos, 5) ? 18.0 : 0.0;

        if (NightHeronEntity.isWaterEdge(level, pos)) {
            score += 16.0;
        }

        // 地面类型加分
        if (below.is(Blocks.WATER) || below.is(Blocks.SEAGRASS)
                || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT_PATH)) {
            score += 5.0;
        }

        // 遮蔽物加分
        if (below.is(BlockTags.WALLS) || below.is(BlockTags.LEAVES)) {
            score += preferRoost ? 24.0 : 8.0;
        }

        // 栖息偏好
        if (preferRoost) {
            score += roostCoverScore(level, pos) * 5.0;
            score += nearbyRoostingNightHeronScore(nightHeron, pos);
        }

        // 远离威胁
        if (threatPosition != null) {
            score += Math.min(28.0, Vec3.atCenterOf(pos).distanceToSqr(threatPosition) * 0.45);
        }

        // 高度惩罚
        score -= Math.abs(pos.getY() - nightHeron.getY()) * 0.25;

        return score;
    }

    // ============ 栖息相关 ============

    /**
     * 检查是否为栖息点
     */
    public static boolean isRoostingSpot(Level level, BlockPos pos) {
        if (!NightHeronEntity.canReadChunk(level, pos)) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        if (below.is(BlockTags.WALLS) || below.is(BlockTags.LEAVES)) {
            return true;
        }

        return roostCoverScore(level, pos) >= 2.0
                && (NightHeronEntity.isWaterEdge(level, pos) || NightHeronEntity.isNearWater(level, pos, 6));
    }

    /**
     * 检查附近是否有栖息遮蔽物
     */
    public static boolean hasRoostCoverNear(Level level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset <= radius * radius) {
                    for (int yOffset = -2; yOffset <= 4; ++yOffset) {
                        mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                        if (NightHeronEntity.canReadChunk(level, mutablePos) && isRoostCoverBlock(level.getBlockState(mutablePos))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 计算栖息遮蔽物分数
     */
    private static double roostCoverScore(Level level, BlockPos pos) {
        double score = 0.0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int xOffset = -5; xOffset <= 5; ++xOffset) {
            for (int zOffset = -5; zOffset <= 5; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset <= 25) {
                    for (int yOffset = -2; yOffset <= 5; ++yOffset) {
                        mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                        if (NightHeronEntity.canReadChunk(level, mutablePos)) {
                            BlockState state = level.getBlockState(mutablePos);
                            if (state.is(BlockTags.WALLS)) {
                                score += 0.9;
                            } else if (state.is(BlockTags.LEAVES)) {
                                score += 0.65;
                            } else if (isRoostCoverBlock(state)) {
                                score += 0.45;
                            }
                        }
                    }
                }
            }
        }
        return Math.min(score, 8.0);
    }

    /**
     * 判断是否为栖息遮蔽物方块
     */
    private static boolean isRoostCoverBlock(BlockState state) {
        return state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.VINE)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CAVE_VINES)
                || state.is(Blocks.FERN)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS);
    }

    /**
     * 计算附近栖息夜鹭的分数
     */
    private static double nearbyRoostingNightHeronScore(NightHeronEntity nightHeron, BlockPos pos) {
        return nightHeron.level().getEntitiesOfClass(
                NightHeronEntity.class,
                nightHeron.getBoundingBox().inflate(12.0),
                other -> other != nightHeron && other.isAlive() && !other.getBehaviorState().isAirborne()
        ).stream().mapToDouble(other -> {
            double distance = Vec3.atCenterOf(pos).distanceToSqr(other.position());
            if (distance < 2.25) {
                return -6.0;
            }
            return distance <= 7.0 ? 9.0 : Math.max(0.0, 7.0 - distance * 0.35);
        }).sum();
    }
}