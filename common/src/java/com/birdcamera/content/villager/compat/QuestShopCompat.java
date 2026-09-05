package com.birdcamera.content.villager.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * QuestShop兼容
 * 当GoldenTweaks和QuestShop同时存在时：emerald -> radiant_gold
 */
public class QuestShopCompat {
   private static final String GOLDENTWEAKS = "goldentweaks";
   private static final String QUESTSHOP = "questshop";
   private static final ResourceLocation BRILLIANT_GOLD_ID = ResourceLocation.fromNamespaceAndPath("goldentweaks", "radiant_gold");
   private static Boolean enabled;

   public static boolean isEnabled() {
      if (enabled == null) {
         enabled = FabricLoader.getInstance().isModLoaded(GOLDENTWEAKS) && FabricLoader.getInstance().isModLoaded(QUESTSHOP);
      }

      return enabled;
   }

   /**
    * 创建村民交易奖励物品：兼容开启时用 radiant_gold，否则用绿宝石
    */
   public static ItemStack createCurrency(int count) {
      if (!isEnabled()) {
         return new ItemStack(Items.EMERALD, count);
      }

      Item item = BuiltInRegistries.ITEM.get(BRILLIANT_GOLD_ID);
      return new ItemStack(item, count);
   }
}