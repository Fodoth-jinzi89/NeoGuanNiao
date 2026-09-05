package com.birdcamera.content.feed;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class GoldenFoodBagItem extends Item {
   public GoldenFoodBagItem(Properties properties) {
      super(properties);
   }

   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      Component name = super.getName(stack);
      return name.copy().withStyle(ChatFormatting.GOLD);
   }
}
