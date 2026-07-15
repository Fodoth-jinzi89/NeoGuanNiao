package net.fodoth.skina.neoguanniao.content.bird.feature.species;

import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSenses;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSpeciesProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * 夜鹭物种配置文件
 * 定义了夜鹭的行为参数、感知逻辑和偏好
 *
 * <p>夜鹭是夜行性水鸟，偏好在水边活动和觅食
 */
public final class NightHeronProfile extends BirdSpeciesProfile {

    public static final NightHeronProfile INSTANCE = new NightHeronProfile();

    private NightHeronProfile() {
    }

    // ============ 性格基础值 ============

    @Override
    public float baseBoldness() {
        return 0.34F;  // 比较谨慎
    }

    @Override
    public float baseWariness() {
        return 0.68F;  // 警惕性较高
    }

    @Override
    public float baseActivity() {
        return 0.52F;  // 中等活跃
    }

    @Override
    public float baseSociability() {
        return 0.38F;  // 不太社交
    }

    @Override
    public float baseFlightiness() {
        return 0.58F;  // 中等易惊
    }

    // ============ 时间感知 ============

    @Override
    public boolean isActiveTime(BirdSenses senses) {
        long time = senses.dayTime();
        // 夜行性：黄昏到黎明（12000-24000 和 0-3000）
        return time >= 12000L || time <= 3000L;
    }

    @Override
    public boolean isRoostTime(BirdSenses senses) {
        long time = senses.dayTime();
        // 白天休息（3000-12000）
        return time > 3000L && time < 12000L;
    }

    // ============ 猎物相关 ============

    @Override
    public boolean isPreferredPrey(LivingEntity entity) {
        return entity instanceof AbstractFish
                || entity.getType() == EntityType.SALMON
                || entity.getType() == EntityType.PUFFERFISH;
    }

    @Override
    @Nullable
    public LivingEntity findNearestPrey(PathfinderMob bird) {
        AABB searchBox = bird.getBoundingBox().inflate(8.0);
        return bird.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                        entity -> entity.isAlive() && this.isPreferredPrey(entity))
                .stream()
                .min(Comparator.comparingDouble(bird::distanceToSqr))
                .orElse(null);
    }

    // ============ 玩家交互 ============

    @Override
    public boolean isTemptingPlayer(Player player, PathfinderMob bird) {
        return player.getMainHandItem().is(Items.COD)
                || player.getMainHandItem().is(Items.SALMON)
                || player.getOffhandItem().is(Items.COD)
                || player.getOffhandItem().is(Items.SALMON);
    }

    // ============ 环境感知 ============

    @Override
    public boolean isNearWater(PathfinderMob bird) {
        return this.scanForWater(bird, 6);
    }

    @Override
    public boolean isWaterEdge(PathfinderMob bird) {
        return this.scanForWater(bird, 3) && this.scanForDryGround(bird, 2);
    }

    @Override
    public boolean isNearCover(PathfinderMob bird) {
        Level level = bird.level();
        BlockPos origin = bird.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-4, -1, -4),
                origin.offset(4, 3, 4)
        )) {
            if (this.isCoverBlock(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNearRoost(PathfinderMob bird) {
        return this.isNearCover(bird);
    }

    // ============ 计算逻辑 ============

    @Override
    public float computeComfort(BirdSenses senses) {
        float comfort = 0.25F;

        if (senses.nearWater()) {
            comfort += 0.25F;
        }
        if (senses.waterEdge()) {
            comfort += 0.25F;
        }
        if (senses.nearCover()) {
            comfort += 0.15F;
        }
        if (senses.nearRoost()) {
            comfort += 0.15F;
        }
        if (senses.hasNearbyThreat()) {
            comfort -= 0.28F;
        }

        return this.clamp(comfort);
    }

    @Override
    public boolean wantsForage(BirdBrain brain) {
        BirdSenses senses = brain.senses();
        return senses.activeTime()
                && senses.isOnGround()
                && senses.nearWater()
                && brain.motivation().hunger() > 0.38F
                && brain.motivation().fear() < 0.55F
                && brain.motivation().fatigue() < 0.85F
                && brain.computeRiskScore() < 0.58F;
    }

    // ============ 私有辅助方法 ============

    /**
     * 扫描附近是否有水
     */
    private boolean scanForWater(PathfinderMob bird, int radius) {
        Level level = bird.level();
        BlockPos origin = bird.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -2, -radius),
                origin.offset(radius, 1, radius)
        )) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 扫描附近是否有干燥地面
     */
    @SuppressWarnings("SameParameterValue")
    private boolean scanForDryGround(PathfinderMob bird, int radius) {
        Level level = bird.level();
        BlockPos origin = bird.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -1, -radius),
                origin.offset(radius, 1, radius)
        )) {
            BlockState state = level.getBlockState(pos);
            // 检查方块碰撞箱不为空，且上方不是水
            if (!state.getCollisionShape(level, pos).isEmpty()
                    && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为遮蔽方块（提供掩护）
     */
    private boolean isCoverBlock(BlockState state) {
        return state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.FERN)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.VINE)
                || state.is(Blocks.CAVE_VINES);
    }
}