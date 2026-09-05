package com.birdcamera.content.feed;

import com.birdcamera.registry.BirdCameraItems;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
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

   public BreadcrumbPileBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LAYERS, 4)).setValue(AGE, 0)).setValue(BITES, 7)
      );
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.MODEL;
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return Shapes.empty();
   }

   public boolean isPossibleToRespawnInThis(@NotNull BlockState state) {
      return true;
   }

   public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext context) {
      Item item = context.getItemInHand().getItem();
      return item == BirdCameraItems.BREADCRUMBS && state.getValue(BITES) < 7 ? true : super.canBeReplaced(state, context);
   }

   @Nullable
   public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      if (state.is(this)) {
         return this.stateForBites(state, Math.min(7, (Integer)state.getValue(BITES) + 7), 0);
      } else {
         FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
         return !fluidState.isEmpty()
            ? null
            : (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, 0)).setValue(BITES, 7)).setValue(LAYERS, 4);
      }
   }

   public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
      super.onPlace(state, level, pos, oldState, movedByPiston);
      if (!level.isClientSide) {
         level.scheduleTick(pos, this, 600);
      }
   }

   @NotNull
   public BlockState updateShape(
      @NotNull BlockState state,
      @NotNull Direction direction,
      @NotNull BlockState neighborState,
      @NotNull LevelAccessor level,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos
   ) {
      return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
      BlockPos belowPos = pos.below();
      BlockState below = level.getBlockState(belowPos);
      return below.isFaceSturdy(level, belowPos, Direction.UP) || below.is(Blocks.FARMLAND);
   }

   public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      if (state.canSurvive(level, pos) && level.getFluidState(pos).isEmpty()) {
         boolean rainingHere = level.isRainingAt(pos.above());
         int ageIncrease = rainingHere ? 2 : 1;
         int newAge = (Integer)state.getValue(AGE) + ageIncrease;
         if (newAge > 5) {
            level.removeBlock(pos, false);
         } else {
            level.setBlock(pos, (BlockState)state.setValue(AGE, newAge), 2);
            level.scheduleTick(pos, this, 600);
         }
      } else {
         level.removeBlock(pos, false);
      }
   }

   public boolean consumeOneServing(Level level, BlockPos pos, BlockState state) {
      BlockState currentState = level.getBlockState(pos);
      if (!currentState.is(this)) {
         return false;
      } else {
         int bites = (Integer)currentState.getValue(BITES);
         if (bites > 1) {
            level.setBlock(pos, this.stateForBites(currentState, bites - 1, 0), 2);
            if (!level.isClientSide) {
               level.scheduleTick(pos, this, 600);
            }
         } else {
            level.removeBlock(pos, false);
         }

         return true;
      }
   }

   private BlockState stateForBites(BlockState state, int bites, int age) {
      return (BlockState)((BlockState)((BlockState)state.setValue(BITES, bites)).setValue(LAYERS, this.layersForBites(bites))).setValue(AGE, age);
   }

   private int layersForBites(int bites) {
      if (bites >= 6) {
         return 4;
      } else if (bites >= 4) {
         return 3;
      } else {
         return bites >= 2 ? 2 : 1;
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{LAYERS, AGE, BITES});
   }
}
