package com.birdcamera.content.villager.trade;

import java.util.List;
import com.birdcamera.content.villager.MerchantOfferBuilder;
import com.birdcamera.content.villager.compat.QuestShopCompat;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

public class BirdBagTrade implements ItemListing {
   private static final List<BirdBagTrade.TradeEntry> BASIC = List.of(
      new BirdBagTrade.TradeEntry(BirdCameraItems.BIRD_FOOD_BAG_SEED, 8, 4), new BirdBagTrade.TradeEntry(BirdCameraItems.BIRD_FOOD_BAG_FISH, 12, 4)
   );
   private static final List<BirdBagTrade.TradeEntry> GREEN = List.of(
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_GROWTH, 16, 4),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_REJUVENATE, 16, 4),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_STOP, 16, 4)
   );
   private static final List<BirdBagTrade.TradeEntry> ADVANCED = List.of(
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_TRANSMUTE, 20, 4),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_SIZE_UP, 10, 4),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_SIZE_DOWN, 10, 4)
   );
   private static final List<BirdBagTrade.TradeEntry> EXPERT = List.of(
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_FAST, 32, 4),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_ADD, 24, 1)
   );
   private static final List<BirdBagTrade.TradeEntry> MASTER = List.of(
      new BirdBagTrade.TradeEntry(BirdCameraItems.GOLDEN_FOOD_BAG_UPGRADE, 64, 1),
      new BirdBagTrade.TradeEntry(BirdCameraItems.GOLDEN_FOOD_BAG_EGG_ADD, 32, 1)
   );

   public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
      int level = this.getVillagerLevel(trader);
      List<BirdBagTrade.TradeEntry> pool = this.getPool(level);
      BirdBagTrade.TradeEntry entry = pool.get(random.nextInt(pool.size()));
      int count = entry.count();
      ItemStack result = new ItemStack(entry.item(), count);
      ItemStack cost = QuestShopCompat.createCurrency(entry.price());
      MerchantOffer offer = new MerchantOffer(new ItemCost(cost.getItem(), cost.getCount()), result, this.getMaxUses(level), this.getExperience(level), 0.05F);
      return MerchantOfferBuilder.of(offer).displayCost(cost).displayResult(result).build();
   }

   private List<BirdBagTrade.TradeEntry> getPool(int level) {
      return switch (level) {
         case 1 -> BASIC;
         case 2 -> GREEN;
         case 3 -> ADVANCED;
         case 4 -> EXPERT;
         default -> MASTER;
      };
   }

   private int getVillagerLevel(Entity entity) {
      return entity instanceof Villager villager ? villager.getVillagerData().getLevel() : 1;
   }

   private int getMaxUses(int level) {
      return switch (level) {
         case 1 -> 16;
         case 2 -> 12;
         case 3 -> 8;
         case 4 -> 6;
         default -> 4;
      };
   }

   private int getExperience(int level) {
      return switch (level) {
         case 1 -> 3;
         case 2 -> 5;
         case 3 -> 8;
         case 4 -> 12;
         default -> 20;
      };
   }

   private static record TradeEntry(Item item, int price, int count) {
   }
}
