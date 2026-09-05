package com.birdcamera.content.cage;

import com.birdcamera.registry.BirdCameraBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟笼方块 - 支持小/中/大三种变体，使用GeckoLib渲染
 */
public class BirdCageBlock extends BaseEntityBlock {

    public static final MapCodec<BirdCageBlock> CODEC = simpleCodec(BirdCageBlock::new);

    public static final EnumProperty<BirdCageVariant> VARIANT = EnumProperty.create("variant", BirdCageVariant.class);

    private final BirdCageVariant defaultVariant;

    public BirdCageBlock(BirdCageVariant variant, Properties properties) {
        super(properties
                .mapColor(MapColor.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.defaultVariant = variant;
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, variant));
    }

    private BirdCageBlock(Properties properties) {
        this(BirdCageVariant.SMALL, properties);
    }

    @Override
    protected MapCodec<? extends BirdCageBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BirdCageVariant variant = state.getValue(VARIANT);
        return variant.getShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BirdCageVariant variant = state.getValue(VARIANT);
        return variant.getShape();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BirdCageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, BirdCameraBlockEntityTypes.BIRD_CAGE, BirdCageBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BirdCageBlockEntity cageEntity) {
                player.openMenu(cageEntity);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BirdCageBlockEntity cageEntity) {
                cageEntity.scatterContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
