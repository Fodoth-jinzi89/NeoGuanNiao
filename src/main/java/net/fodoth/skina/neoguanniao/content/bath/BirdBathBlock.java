package net.fodoth.skina.neoguanniao.content.bath;

import com.mojang.serialization.MapCodec;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BirdBathBlock extends BaseEntityBlock {

    private final BirdBathVariant variant;

    public BirdBathBlock(BirdBathVariant variant, BlockBehaviour.Properties properties) {
        super(properties);
        this.variant = variant;
    }

    public BirdBathVariant variant() {
        return variant;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public final MapCodec<BirdBathBlock> CODEC =
            simpleCodec(properties ->
                    new BirdBathBlock(
                            this.variant(),
                            properties
                    )
            );

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
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
    public @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BirdBathBlockEntity birdBath)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.WATER_BUCKET)) {
            return fillWithWaterBucket(level, pos, player, hand, stack, birdBath);
        } else if (isWaterBottle(stack)) {
            return addStackContent(
                    level, pos, player, hand, stack, birdBath,
                    BirdBathContentType.WATER,
                    new ItemStack(Items.GLASS_BOTTLE)
            );
        } else {
            BirdBathContentType contentType = contentTypeForStack(stack);
            return !contentType.isEmpty()
                    ? addStackContent(level, pos, player, hand, stack, birdBath, contentType, ItemStack.EMPTY)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hitResult
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BirdBathBlockEntity birdBath)) {
            return InteractionResult.PASS;
        }

        return handleEmptyHand(level, pos, player, birdBath);
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BirdBathBlockEntity birdBath) {
            birdBath.environmentTick(level, pos, state, random);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BirdBathBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                NeoGuanNiaoBlockEntityTypes.BIRD_BATH.get(),
                BirdBathBlockEntity::serverTick
        );
    }

    private InteractionResult handleEmptyHand(
            Level level,
            BlockPos pos,
            Player player,
            BirdBathBlockEntity birdBath
    ) {
        if (birdBath.isSpoiled()) {
            if (!level.isClientSide && birdBath.cleanByHand()) {
                BirdBathEffects.spoiledCleared(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (player.isShiftKeyDown()) {
            if (birdBath.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && birdBath.clearContent()) {
                BirdBathEffects.contentCleared(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (birdBath.isDirty()) {
            BirdBathCleanliness previous = birdBath.getCleanliness();
            if (!level.isClientSide && birdBath.cleanByHand()) {
                BirdBathEffects.cleaned(level, pos, previous);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private ItemInteractionResult fillWithWaterBucket(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BirdBathBlockEntity birdBath
    ) {
        if (birdBath.canAccept(BirdBathContentType.WATER)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (birdBath.getContentType() == BirdBathContentType.WATER && birdBath.getContentLevel() >= 3) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            if (birdBath.getCleanliness() == BirdBathCleanliness.FILTHY) {
                birdBath.cleanByHand();
            }
            birdBath.setContent(BirdBathContentType.WATER, 3);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                giveOrReplaceHeldItem(player, hand, new ItemStack(Items.BUCKET));
            }
            BirdBathEffects.waterAdded(level, pos, SoundEvents.BUCKET_EMPTY);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private ItemInteractionResult addStackContent(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BirdBathBlockEntity birdBath,
            BirdBathContentType contentType,
            ItemStack remainder
    ) {
        if (birdBath.canAccept(contentType)) {
            return canReplaceContent(birdBath, contentType)
                    ? this.replaceStackContent(level, pos, player, hand, stack, birdBath, contentType, remainder)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (birdBath.getContentLevel() >= 3) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && birdBath.addContent(contentType, 1)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!remainder.isEmpty()) {
                    giveOrReplaceHeldItem(player, hand, remainder.copy());
                }
            }
            if (contentType == BirdBathContentType.WATER) {
                BirdBathEffects.waterAdded(level, pos, SoundEvents.BOTTLE_EMPTY);
            } else {
                BirdBathEffects.foodAdded(level, pos, contentType);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private ItemInteractionResult replaceStackContent(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BirdBathBlockEntity birdBath,
            BirdBathContentType contentType,
            ItemStack remainder
    ) {
        if (!level.isClientSide && birdBath.setContent(contentType, 1)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!remainder.isEmpty()) {
                    giveOrReplaceHeldItem(player, hand, remainder.copy());
                }
            }
            BirdBathEffects.foodAdded(level, pos, contentType);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean canReplaceContent(BirdBathBlockEntity birdBath, BirdBathContentType contentType) {
        if (birdBath != null && contentType != null && contentType.isFood()
                && !birdBath.isSpoiled() && !birdBath.isEmpty()) {
            BirdBathContentType currentType = birdBath.getContentType();
            return currentType != contentType && (currentType.isWaterLike() || currentType.isFood());
        }
        return false;
    }

    private static BirdBathContentType contentTypeForStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return BirdBathContentType.EMPTY;
        }
        if (isFish(stack)) {
            return BirdBathContentType.FISH;
        }
        if (isMeat(stack)) {
            return BirdBathContentType.MEAT;
        }
        if (stack.is(Items.BREAD) || stack.is(NeoGuanNiaoItems.BREADCRUMBS.get())) {
            return BirdBathContentType.BREAD;
        }
        return BirdBathContentType.EMPTY;
    }

    private static boolean isFish(ItemStack stack) {
        return stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.TROPICAL_FISH)
                || stack.is(Items.PUFFERFISH)
                || stack.is(Items.COOKED_COD)
                || stack.is(Items.COOKED_SALMON);
    }

    private static boolean isMeat(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.MUTTON)
                || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.RABBIT)
                || stack.is(Items.COOKED_RABBIT);
    }

    @SuppressWarnings("deprecation")
    private static boolean isWaterBottle(ItemStack stack) {
        PotionContents contents =
                stack.get(DataComponents.POTION_CONTENTS);

        return contents != null
                && contents.potion()
                .map(holder -> holder.is(Potions.WATER))
                .orElse(false);
    }

    private static void giveOrReplaceHeldItem(Player player, InteractionHand hand, ItemStack replacement) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            player.setItemInHand(hand, replacement);
        } else if (!player.getInventory().add(replacement)) {
            player.drop(replacement, false);
        }
    }
}