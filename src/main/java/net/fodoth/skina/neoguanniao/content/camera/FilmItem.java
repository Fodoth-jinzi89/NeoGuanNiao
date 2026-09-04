package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.camera.PhotographClientActions;
import net.fodoth.skina.neoguanniao.content.camera.LegacyPhotoMigration;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import java.util.List;
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
import org.jetbrains.annotations.Nullable;

public class FilmItem
extends Item {
    public FilmItem(Item.Properties properties) {
        super(properties);
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        LegacyPhotoMigration.queue(level, stack);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
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

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!PhotographData.hasImage(stack)) {
            tooltip.add((Component)Component.translatable((String)"item.neoguanniao.film.tooltip.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        String photographer = PhotographData.photographer(stack);
        if (!photographer.isEmpty()) {
            tooltip.add((Component)Component.translatable((String)"item.neoguanniao.photograph.tooltip.photographer", (Object[])new Object[]{photographer}).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add((Component)Component.translatable((String)"item.neoguanniao.film.tooltip.frame").withStyle(ChatFormatting.DARK_GRAY));
    }

}

