package net.fodoth.skina.neoguanniao.content.feed;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GreenFoodBagItem extends Item {

    public GreenFoodBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component name = super.getName(stack);
        return name.copy().withStyle(ChatFormatting.GREEN);
    }
}
