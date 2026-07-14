package net.fodoth.skina.neoguanniao.content.bird.feature.species;

import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSenses;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSpeciesProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 麻雀物种配置文件
 * 定义了麻雀的行为参数、感知逻辑和偏好
 * <p>
 * 麻雀是日行性鸟类，高度社交，偏好人类居住区活动
 */
public final class SparrowProfile extends BirdSpeciesProfile {

    public static final SparrowProfile INSTANCE = new SparrowProfile();

    private SparrowProfile() {
    }

    // ============ 性格基础值 ============

    @Override
    public float baseBoldness() {
        return 0.42F;  // 中等胆量
    }

    @Override
    public float baseWariness() {
        return 0.55F;  // 中等警惕
    }

    @Override
    public float baseActivity() {
        return 0.68F;  // 较为活跃
    }

    @Override
    public float baseSociability() {
        return 0.72F;  // 高度社交
    }

    @Override
    public float baseFlightiness() {
        return 0.52F;  // 中等易惊
    }

    // ============ 动机参数 ============

    @Override
    public float hungerGainPerTick(BirdSenses senses) {
        return senses.activeTime() ? 2.0E-4F : 7.0E-5F;
    }

    // ============ 时间感知 ============

    @Override
    public boolean isActiveTime(BirdSenses senses) {
        long time = senses.dayTime();
        // 日行性：黄昏到第二天中午（23000-24000 和 0-12500）
        return time >= 23000L || time < 12500L;
    }

    @Override
    public boolean isRoostTime(BirdSenses senses) {
        return !this.isActiveTime(senses);
    }

    // ============ 猎物相关 ============

    @Override
    public boolean isPreferredPrey(LivingEntity entity) {
        return false;  // 麻雀不捕食其他生物
    }

    @Override
    @Nullable
    public LivingEntity findNearestPrey(PathfinderMob bird) {
        return null;  // 麻雀不捕食其他生物
    }

    // ============ 玩家交互 ============

    @Override
    public boolean isTemptingPlayer(Player player) {
        return player.getMainHandItem().is(Items.WHEAT_SEEDS)
                || player.getMainHandItem().is(Items.MELON_SEEDS)
                || player.getMainHandItem().is(Items.PUMPKIN_SEEDS)
                || player.getMainHandItem().is(Items.BEETROOT_SEEDS)
                || player.getMainHandItem().is(Items.TORCHFLOWER_SEEDS)
                || player.getMainHandItem().is(Items.PITCHER_POD)
                || player.getOffhandItem().is(Items.WHEAT_SEEDS)
                || player.getOffhandItem().is(Items.MELON_SEEDS)
                || player.getOffhandItem().is(Items.PUMPKIN_SEEDS)
                || player.getOffhandItem().is(Items.BEETROOT_SEEDS)
                || player.getOffhandItem().is(Items.TORCHFLOWER_SEEDS)
                || player.getOffhandItem().is(Items.PITCHER_POD);
    }

    // ============ 环境感知 ============

    @Override
    public boolean isNearWater(PathfinderMob bird) {
        return false;  // 麻雀不特别依赖水域
    }

    @Override
    public boolean isWaterEdge(PathfinderMob bird) {
        return false;  // 麻雀不特别依赖水域
    }

    @Override
    public boolean isNearCover(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 5, 3, this::isCoverBlock);
    }

    @Override
    public boolean isNearRoost(PathfinderMob bird) {
        return this.scanNearbyBlocks(bird, 7, 5, this::isRoostBlock);
    }

    // ============ 计算逻辑 ============

    @Override
    public float computeComfort(BirdSenses senses) {
        float comfort = 0.34F;

        if (senses.nearCover()) {
            comfort += 0.24F;
        }
        if (senses.nearRoost()) {
            comfort += 0.22F;
        }
        if (senses.roostTime()) {
            comfort -= 0.16F;
        }
        if (senses.hasNearbyThreat()) {
            comfort -= 0.26F;
        }

        return this.clamp(comfort);
    }

    @Override
    public boolean wantsForage(BirdBrain brain) {
        BirdSenses senses = brain.senses();
        return senses.activeTime()
                && senses.isOnGround()
                && brain.motivation().hunger() > 0.35F
                && brain.motivation().fear() < 0.55F
                && brain.motivation().fatigue() < 0.85F
                && brain.computeRiskScore() < 0.6F;
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
                || state.is(Blocks.TALL_SEAGRASS)
                || state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof FenceBlock
                || state.is(Blocks.FARMLAND)
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
                || state.is(Blocks.FARMLAND)
                || state.getBlock() instanceof BedBlock
                || state.getBlock() instanceof DoorBlock;
    }

    // ============ 函数式接口 ============

    @FunctionalInterface
    private interface BlockPredicate {
        boolean test(BlockState state);
    }
}