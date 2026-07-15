package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid;

import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSenses;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSpeciesProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 斑鸠物种配置文件
 * 定义了斑鸠的行为参数、感知逻辑和偏好
 *
 * <p>斑鸠是日行性鸟类，性格温和，偏好乡村和开阔地带
 */
public final class SpottedDoveProfile extends BirdSpeciesProfile {

    public static final SpottedDoveProfile INSTANCE = new SpottedDoveProfile();

    private SpottedDoveProfile() {
    }

    // ============ 性格基础值 ============

    @Override
    public float baseBoldness() {
        return 0.46F;  // 中等偏谨慎
    }

    @Override
    public float baseWariness() {
        return 0.48F;  // 中等警惕
    }

    @Override
    public float baseActivity() {
        return 0.58F;  // 中等活跃
    }

    @Override
    public float baseSociability() {
        return 0.62F;  // 中等偏社交
    }

    @Override
    public float baseFlightiness() {
        return 0.38F;  // 不易受惊
    }

    // ============ 时间感知 ============

    @Override
    public boolean isActiveTime(BirdSenses senses) {
        long time = senses.dayTime();
        // 日行性：黄昏到第二天中午稍早（23000-24000 和 0-12000）
        return time >= 23000L || time < 12000L;
    }

    @Override
    public boolean isRoostTime(BirdSenses senses) {
        return !this.isActiveTime(senses);
    }

    // ============ 猎物相关 ============

    @Override
    public boolean isPreferredPrey(LivingEntity entity) {
        return false;  // 斑鸠不捕食其他生物
    }

    // ============ 玩家交互 ============

    @Override
    public boolean isTemptingPlayer(Player player) {
        return AbstractColumbidEntity.isSeedFood(player.getMainHandItem())
                || AbstractColumbidEntity.isSeedFood(player.getOffhandItem());
    }

    // ============ 环境感知 ============

    @Override
    public boolean isNearCover(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 7, 4, this::isCoverBlock);
    }

    @Override
    public boolean isNearRoost(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 9, 6, this::isRoostBlock);
    }

    // ============ 计算逻辑 ============

    @Override
    public float computeComfort(BirdSenses senses) {
        float comfort = 0.36F;

        if (senses.nearCover()) {
            comfort += 0.23F;
        }
        if (senses.nearRoost()) {
            comfort += 0.2F;
        }
        if (senses.roostTime()) {
            comfort -= 0.12F;
        }
        if (senses.hasNearbyThreat()) {
            comfort -= 0.24F;
        }

        return this.clamp(comfort);
    }

    @Override
    public boolean wantsForage(BirdBrain brain) {
        BirdSenses senses = brain.senses();
        return senses.activeTime()
                && senses.isOnGround()
                && brain.motivation().hunger() > 0.3F
                && brain.motivation().fear() < 0.55F
                && brain.computeRiskScore() < 0.65F;
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
     * 判断是否为遮蔽方块（提供掩护）
     */
    private boolean isCoverBlock(BlockState state) {
        return state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.FERN)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.GRASS_BLOCK)
                || state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof ComposterBlock;
    }

    /**
     * 判断是否为栖息方块（适合睡觉的地方）
     */
    private boolean isRoostBlock(BlockState state) {
        return state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.is(Blocks.GRASS_BLOCK);
    }

    // ============ 函数式接口 ============

    @FunctionalInterface
    private interface BlockPredicate {
        boolean test(BlockState state);
    }
}