package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.camera.PhotographClientActions;
import java.util.List;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FilmItem
extends Item {
    private static final DateTimeFormatter CAPTURE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static Component formatTime(long ticks) {
        return Component.literal(CAPTURE_TIME_FORMAT.format(Instant.ofEpochMilli(Math.max(0L, ticks)).atZone(ZoneId.systemDefault())));
    }
    public FilmItem(Item.Properties properties) {
        super(properties);
    }

    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide && PhotographData.hasImage(stack)) {
            ItemStack copy = stack.copy();
            try {
                PhotographClientActions.openScreen(copy);
            } catch (RuntimeException exception) {
                NeoGuanNiao.LOGGER.error("Unable to open film preview", exception);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (!PhotographData.hasImage(stack)) {
            tooltip.add(Component.translatable("item.neoguanniao.film.tooltip.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        String photographer = PhotographData.photographer(stack);
        tooltip.add(Component.translatable("tooltip.neoguanniao.photo.time").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(formatTime(PhotographData.captureTime(stack)).getString()).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("tooltip.neoguanniao.photo.dimension").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(PhotographData.dimension(stack)).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("tooltip.neoguanniao.photo.coordinates").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.format("%s, %s, %s", PhotographData.x(stack), PhotographData.y(stack), PhotographData.z(stack))).withStyle(ChatFormatting.AQUA)));
        if (!photographer.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.neoguanniao.photo.photographer").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(photographer).withStyle(ChatFormatting.AQUA)));
        }
        tooltip.add(Component.translatable("item.neoguanniao.film.tooltip.frame").withStyle(ChatFormatting.DARK_GRAY));
    }

}

