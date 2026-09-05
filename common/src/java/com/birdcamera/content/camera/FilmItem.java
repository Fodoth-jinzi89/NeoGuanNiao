package com.birdcamera.content.camera;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 胶片物品（迁移自 guaniao-2.1.3）。拍摄所得照片保存在胶片 NBT 中，可查看、可合成相框相片。
 */
public class FilmItem extends Item {
    public FilmItem(Properties properties) {
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
                throw new IllegalStateException("Failed to open film preview", e);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!PhotographData.hasImage(stack)) {
            tooltip.add(Component.translatable("item.birdcamera.film.tooltip.empty").withStyle(ChatFormatting.GRAY));
        } else {
            String photographer = PhotographData.photographer(stack);
            if (!photographer.isEmpty()) {
                tooltip.add(Component.translatable("item.birdcamera.photograph.tooltip.photographer", photographer).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("item.birdcamera.film.tooltip.frame").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}