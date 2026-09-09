package net.fodoth.skina.neoguanniao.content.cage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BirdCageBlock extends BaseEntityBlock {


    private final BirdCageVariant variant;


    public BirdCageBlock(
            BirdCageVariant variant,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.variant = variant;
    }


    public BirdCageVariant variant() {
        return variant;
    }


    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public final MapCodec<BirdCageBlock> CODEC =
            simpleCodec(properties ->
                    new BirdCageBlock(
                            this.variant(),
                            properties
                    )
            );

    @Override
    public @NotNull RenderShape getRenderShape(
            @NotNull BlockState state
    ) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }



    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return variant.shape();
    }



    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        return new BirdCageBlockEntity(
                pos,
                state
        );
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof BirdCageBlockEntity cage) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag.contains("CapturedBird")) cage.setCapturedBird(tag.getCompound("CapturedBird"));
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BirdCageBlockEntity cage)) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && !cage.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack item = new ItemStack(this);
                CompoundTag bird = cage.removeCapturedBird();
                CustomData.update(DataComponents.CUSTOM_DATA, item, t -> t.put("CapturedBird", bird));
                if (!player.getInventory().add(item)) player.drop(item, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
