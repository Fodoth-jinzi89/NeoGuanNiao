package com.birdcamera.content.nest;

import com.mojang.serialization.MapCodec;
import com.birdcamera.registry.BirdCameraBlockEntityTypes;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BirdNestBlock extends BaseEntityBlock {
   private static final VoxelShape SHAPE = Shapes.or(
      Block.box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0),
      new VoxelShape[]{
         Block.box(2.0, 2.0, 2.0, 14.0, 5.0, 3.0),
         Block.box(2.0, 2.0, 13.0, 14.0, 5.0, 14.0),
         Block.box(2.0, 2.0, 3.0, 3.0, 5.0, 13.0),
         Block.box(13.0, 2.0, 3.0, 14.0, 5.0, 13.0)
      }
   );
   public static final MapCodec<BirdNestBlock> CODEC = simpleCodec(BirdNestBlock::new);

   public BirdNestBlock(Properties properties) {
      super(properties);
   }

   @NotNull
   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @Nullable
   public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
      return new BirdNestBlockEntity(pos, state);
   }

   @NotNull
   protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return SHAPE;
   }

   @NotNull
   protected ItemInteractionResult useItemOn(
      @NotNull ItemStack stack,
      @NotNull BlockState state,
      Level level,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (level.isClientSide) {
         return ItemInteractionResult.SUCCESS;
      } else if (level.getBlockEntity(pos) instanceof BirdNestBlockEntity nest) {
         if (stack.is((Item)BirdCameraItems.BIRD_EGG)) {
            for (int i = 0; i < nest.getContainerSize(); i++) {
               if (nest.getItem(i).isEmpty()) {
                  nest.setItem(i, stack.copyWithCount(1));
                  if (!player.getAbilities().instabuild) {
                     stack.shrink(1);
                  }

                  return ItemInteractionResult.SUCCESS;
               }
            }

            return ItemInteractionResult.FAIL;
         } else {
            for (int ix = nest.getContainerSize() - 1; ix >= 0; ix--) {
               ItemStack egg = nest.getItem(ix);
               if (!egg.isEmpty()) {
                  nest.setItem(ix, ItemStack.EMPTY);
                  if (!player.addItem(egg)) {
                     player.drop(egg, false);
                  }

                  return ItemInteractionResult.SUCCESS;
               }
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
      Containers.dropContentsOnDestroy(state, newState, level, pos);
      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
      return type == BirdCameraBlockEntityTypes.BIRD_NEST ? (level1, pos, state1, be) -> {
         if (level1.getGameTime() % 20L == 0L && be instanceof BirdNestBlockEntity nest) {
            nest.tickEggs();
         }
      } : null;
   }
}
