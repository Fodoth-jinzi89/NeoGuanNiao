package net.fodoth.skina.neoguanniao.content.nest;

import com.mojang.serialization.MapCodec;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟巢方块 - 可存放和展示鸟蛋的装饰性方块
 * 交互逻辑：手持鸟蛋右键放入，空手右键取出最后一个鸟蛋
 */
public class BirdNestBlock extends BaseEntityBlock {

    // ==================== 碰撞箱 ====================

    /** 鸟巢的碰撞箱/选择箱：底部+四周边框 */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 2, 14),   // 底部托盘
            Block.box(2, 2, 2, 14, 5, 3),   // 北侧围边
            Block.box(2, 2, 13, 14, 5, 14), // 南侧围边
            Block.box(2, 2, 3, 3, 5, 13),   // 西侧围边
            Block.box(13, 2, 3, 14, 5, 13)  // 东侧围边
    );

    public static final MapCodec<BirdNestBlock> CODEC = simpleCodec(BirdNestBlock::new);

    public BirdNestBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // ==================== 基础方法 ====================

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** 使用Geo模型渲染，不渲染原版方块模型 */
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BirdNestBlockEntity(pos, state);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    // ==================== 交互逻辑 ====================

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof BirdNestBlockEntity nest)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStackHandler inv = nest.getInventory();

        // ----- 手持鸟蛋 → 放入巢穴 -----
        if (stack.is(NeoGuanNiaoItems.BIRD_EGG.get())) {
            for (int i = 0; i < 4; i++) {
                if (inv.getStackInSlot(i).isEmpty()) {
                    nest.setItem(i, stack.copyWithCount(1)); // 放入1个蛋
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1); // 生存模式消耗物品
                    }
                    nest.setChanged();
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.FAIL; // 巢已满
        }

        // ----- 空手或持其他物品 → 取出最后一个鸟蛋（从后向前取）-----
        for (int i = 3; i >= 0; i--) {
            ItemStack egg = nest.getItem(i);
            if (!egg.isEmpty()) {
                nest.setItem(i, ItemStack.EMPTY);
                if (!player.addItem(egg)) {
                    player.drop(egg, false); // 背包满则掉落
                }
                nest.setChanged();
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // ==================== 方块破坏处理 ====================

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BirdNestBlockEntity nest && !level.isClientSide) {
                // 掉落所有鸟蛋
                for (int i = 0; i < nest.getContainerSize(); i++) {
                    ItemStack egg = nest.getItem(i);
                    if (!egg.isEmpty()) {
                        Block.popResource(level, pos, egg.copy());
                    }
                }
                level.removeBlockEntity(pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ==================== 方块刻（Tick） ====================

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (type == NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get()) {
            return (level1, pos, state1, be) -> {
                // 每秒（20 tick）执行一次孵化逻辑
                if (level1.getGameTime() % 20 == 0 && be instanceof BirdNestBlockEntity nest) {
                    nest.tickEggs();
                }
            };
        }
        return null;
    }
}