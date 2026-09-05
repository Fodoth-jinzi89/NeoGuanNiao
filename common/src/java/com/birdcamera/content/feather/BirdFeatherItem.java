package com.birdcamera.content.feather;

import java.util.List;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import com.birdcamera.registry.BirdCameraDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import org.jetbrains.annotations.NotNull;

public class BirdFeatherItem extends Item {
   public BirdFeatherItem(Properties properties) {
      super(properties);
   }

   public static void setFeatherData(ItemStack stack, BirdFeatherData data) {
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_FEATHER_DATA, data);
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_FEATHER_BIRD_TYPE, data.toTypeInt());
      stack.set((DataComponentType)BirdCameraDataComponents.BIRD_FEATHER_SKIN_RARITY, data.rarity());
   }

   public static BirdFeatherData getFeatherData(ItemStack stack) {
      return (BirdFeatherData)stack.get((DataComponentType)BirdCameraDataComponents.BIRD_FEATHER_DATA);
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      BirdFeatherData data = getFeatherData(stack);
      if (data == null) {
         tooltip.add(Component.translatable("tooltip.birdcamera.empty_feather"));
      } else {
         tooltip.add(
            Component.translatable("tooltip.birdcamera.bird_type")
               .append(Component.translatable("entity." + data.birdType().getNamespace() + "." + data.birdType().getPath()))
         );
         BirdSkinRarity rarity = BirdSkinRarity.fromValue(data.rarity());
         Component rarityText = Component.translatable("tooltip.birdcamera.rarity." + rarity.getTranslationKey()).withStyle(rarity.getChatColor());
         tooltip.add(Component.translatable("tooltip.birdcamera.rarity").append(rarityText));
      }
   }

   @NotNull
   public Component getName(@NotNull ItemStack stack) {
      Component name = super.getName(stack);
      BirdFeatherData data = getFeatherData(stack);
      if (data == null) {
         return name;
      } else {
         BirdSkinRarity rarity = BirdSkinRarity.fromValue(data.rarity());
         return name.copy().withStyle(rarity.getChatColor());
      }
   }
}
