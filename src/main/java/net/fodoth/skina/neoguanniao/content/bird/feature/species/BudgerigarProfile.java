package net.fodoth.skina.neoguanniao.content.bird.feature.species;

import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSenses;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSpeciesProfile;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 虎皮鹦鹉物种配置文件
 * 定义了虎皮鹦鹉的行为参数、感知逻辑和偏好
 */
public final class BudgerigarProfile extends BirdSpeciesProfile {

    public static final BudgerigarProfile INSTANCE = new BudgerigarProfile();

    private BudgerigarProfile() {
    }

    // ============ 性格基础值 ============

    @Override
    public float baseBoldness() {
        return 0.82F;  // 非常大胆
    }

    @Override
    public float baseWariness() {
        return 0.16F;  // 警惕性很低
    }

    @Override
    public float baseActivity() {
        return 0.76F;  // 非常活跃
    }

    @Override
    public float baseSociability() {
        return 0.88F;  // 高度社交
    }

    @Override
    public float baseFlightiness() {
        return 0.24F;  // 不易受惊
    }

    // ============ 动机参数 ============

    @Override
    public float hungerGainPerTick(BirdSenses senses) {
        return senses.activeTime() ? 2.2E-4F : 6.0E-5F;
    }

    // ============ 时间感知 ============

    @Override
    public boolean isActiveTime(BirdSenses senses) {
        long time = senses.dayTime();
        return time >= 23000L || time < 11500L;
    }

    @Override
    public boolean isRoostTime(BirdSenses senses) {
        long time = senses.dayTime();
        return time >= 11500L && time < 23000L;
    }

    // ============ 猎物相关 ============

    @Override
    public boolean isPreferredPrey(LivingEntity entity) {
        return false;
    }

    @Override
    @Nullable
    public LivingEntity findNearestPrey(PathfinderMob bird) {
        return super.findNearestPrey(bird);
    }

    // ============ 玩家交互 ============

    @Override
    public boolean isTemptingPlayer(Player player, PathfinderMob bird) {
        if (bird instanceof BudgerigarEntity b) {
            return b.getEatingController().isEdibleFood(player.getMainHandItem())
                    || b.getEatingController().isEdibleFood(player.getOffhandItem());
        }
        return false;
    }

    // ============ 环境感知 ============

    @Override
    public boolean isNearWater(PathfinderMob bird) {
        return super.isNearWater(bird);
    }

    @Override
    public boolean isWaterEdge(PathfinderMob bird) {
        return super.isWaterEdge(bird);
    }

    @Override
    public boolean isNearCover(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 6, 3, this::isComfortBlock);
    }

    @Override
    public boolean isNearRoost(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 7, 5, this::isRoostBlock);
    }

    // ============ 计算逻辑 ============

    @Override
    public float computeComfort(BirdSenses senses) {
        float comfort = 0.36F;

        if (senses.nearCover()) {
            comfort += 0.24F;
        }
        if (senses.nearRoost()) {
            comfort += 0.18F;
        }
        if (senses.activeTime()) {
            comfort += 0.08F;
        }
        if (senses.roostTime()) {
            comfort -= 0.14F;
        }
        if (senses.temptingPlayerNearby() && senses.nearestPlayerDistance() > 3.0) {
            comfort += 0.05F;
        }
        if (senses.hasNearbyThreat()) {
            comfort -= 0.06F;
        }

        return this.clamp(comfort);
    }

    @Override
    public boolean wantsForage(BirdBrain brain) {
        BirdSenses senses = brain.senses();
        PathfinderMob bird = brain.bird();

        // 虎皮鹦鹉在音乐或睡眠时不觅食
        if (bird instanceof BudgerigarEntity budgerigar) {
            if (budgerigar.isBusyWithMusicOrSleep()) {
                return false;
            }
        }

        return senses.activeTime()
                && senses.isOnGround()
                && brain.motivation().hunger() > 0.3F
                && brain.motivation().fear() < 0.72F
                && brain.computeRiskScore() < 0.78F;
    }

    // ============ 私有辅助方法 ============

    /**
     * 扫描附近方块是否匹配条件
     */
    private boolean scanNearbyBlocks(PathfinderMob bird, int horizontalRadius, int verticalRadius, BlockPredicate predicate) {
        Level level = bird.level();
        BlockPos origin = bird.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-horizontalRadius, -1, -horizontalRadius),
                origin.offset(horizontalRadius, verticalRadius, horizontalRadius)
        )) {
            if (predicate.test(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为舒适方块（提供遮蔽和安全感）
     */
    private boolean isComfortBlock(BlockState state) {
        // 草和植物
        if (state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS)
                || state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.FARMLAND)
                || state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof BushBlock) {
            return true;
        }

        // 所有花盆中的植物
        return state.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock;
    }

    /**
     * 判断是否为栖息方块（适合睡觉的地方）
     */
    private boolean isRoostBlock(BlockState state) {
        return state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.is(Blocks.FARMLAND);
    }

    // ============ 函数式接口 ============

    @FunctionalInterface
    private interface BlockPredicate {
        boolean test(BlockState state);
    }
}