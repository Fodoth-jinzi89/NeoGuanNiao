package net.fodoth.skina.neoguanniao.content.feed;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreadcrumbPileBlock extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 4);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 5);
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 1, 7);
    private static final int TICK_INTERVAL = 600;
    private static final int MAX_AGE = 5;
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Shapes.empty(),
            Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0),
            Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0),
            Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0)
    };

    public BreadcrumbPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(LAYERS, 4)
                        .setValue(AGE, 0)
                        .setValue(BITES, 7)
        );
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public boolean isPossibleToRespawnInThis(@NotNull BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext context) {
        Item item = context.getItemInHand().getItem();
        if (item == NeoGuanNiaoItems.BREADCRUMBS.get() && state.getValue(BITES) < 7) {
            return true;
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(this)) {
            return this.stateForBites(state, Math.min(7, state.getValue(BITES) + 7), 0);
        }
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        if (!fluidState.isEmpty()) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(AGE, 0)
                .setValue(BITES, 7)
                .setValue(LAYERS, 4);
    }

    @Override
    public void onPlace(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    public @NotNull BlockState updateShape(
            @NotNull BlockState state,
            @NotNull Direction direction,
            @NotNull BlockState neighborState,
            @NotNull LevelAccessor level,
            @NotNull BlockPos pos,
            @NotNull BlockPos neighborPos
    ) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP) || below.is(Blocks.FARMLAND);
    }

    @Override
    public void tick(
            @NotNull BlockState state,
            @NotNull ServerLevel level,
            @NotNull BlockPos pos,
            @NotNull RandomSource random
    ) {
        if (state.canSurvive(level, pos) && level.getFluidState(pos).isEmpty()) {
            boolean rainingHere = level.isRainingAt(pos.above());
            int ageIncrease = rainingHere ? 2 : 1;
            int newAge = state.getValue(AGE) + ageIncrease;
            if (newAge > MAX_AGE) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(AGE, newAge), 2);
                level.scheduleTick(pos, this, TICK_INTERVAL);
            }
        } else {
            level.removeBlock(pos, false);
        }
    }

    public boolean consumeOneServing(Level level, BlockPos pos, BlockState state) {
        BlockState currentState = level.getBlockState(pos);
        if (!currentState.is(this)) {
            return false;
        }
        int bites = currentState.getValue(BITES);
        if (bites > 1) {
            level.setBlock(pos, this.stateForBites(currentState, bites - 1, 0), 2);
            if (!level.isClientSide) {
                level.scheduleTick(pos, this, TICK_INTERVAL);
            }
        } else {
            level.removeBlock(pos, false);
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private BlockState stateForBites(BlockState state, int bites, int age) {
        return state.setValue(BITES, bites)
                .setValue(LAYERS, this.layersForBites(bites))
                .setValue(AGE, age);
    }

    private int layersForBites(int bites) {
        if (bites >= 6) {
            return 4;
        } else if (bites >= 4) {
            return 3;
        } else if (bites >= 2) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS, AGE, BITES);
    }
}