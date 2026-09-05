package com.birdcamera.content.camera;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 相框相片物品（迁移自 guaniao-2.1.3）。右键查看，对墙右键悬挂为实体。
 */
public class PhotographItem extends Item {
    public PhotographItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        LegacyPhotoMigration.queue(level, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide && PhotographData.hasImage(stack)) {
            ItemStack copy = stack.copy();
            try {
                Class.forName("com.birdcamera.client.camera.PhotographClientActions")
                        .getMethod("openScreen", ItemStack.class)
                        .invoke(null, copy);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to open photograph screen", e);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        if (player != null && PhotographData.hasImage(stack) && face.getAxis().isHorizontal()) {
            BlockPos placePos = context.getClickedPos().relative(face);
            if (!level.isClientSide) {
                LegacyPhotoMigration.migrateNow(level, stack);
                ItemStack photo = stack.copy();
                photo.setCount(1);
                LegacyPhotoMigration.queue(level, photo);
                PhotographEntity entity = new PhotographEntity(level, placePos, face, photo);
                if (!entity.survives()) {
                    return InteractionResult.FAIL;
                }
                level.addFreshEntity(entity);
                level.playSound(null, placePos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.1F);
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!PhotographData.hasImage(stack)) {
            tooltip.add(Component.translatable("item.birdcamera.photograph.tooltip.empty").withStyle(ChatFormatting.GRAY));
        } else {
            String photographer = PhotographData.photographer(stack);
            if (!photographer.isEmpty()) {
                tooltip.add(Component.translatable("item.birdcamera.photograph.tooltip.photographer", photographer).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("item.birdcamera.photograph.tooltip.actions").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}