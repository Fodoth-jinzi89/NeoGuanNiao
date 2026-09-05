package com.birdcamera.content.villager.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import com.birdcamera.content.feather.BirdFeatherData;
import com.birdcamera.content.feather.BirdFeatherItem;
import com.birdcamera.content.villager.MerchantOfferBuilder;
import com.birdcamera.content.villager.compat.QuestShopCompat;
import com.birdcamera.registry.BirdCameraBirdData;
import com.birdcamera.registry.BirdCameraDataComponents;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

public class BirdFeatherTrade implements ItemListing {
   private static final int MAX_RETRY = 10;
   private static final BirdSkinRarity FALLBACK_RARITY = BirdSkinRarity.COMMON;

   public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
      int level = this.getVillagerLevel(trader);
      BirdSkinRarity rarity = this.selectValidRarity(level, random);
      int multiplier = random.nextInt(4) + 1;
      int featherCount = this.getFeatherCountForTrade(rarity) * multiplier;
      int priceCount = this.getPrice(rarity) * multiplier;
      BirdFeatherTrade.FeatherTradeData tradeData = this.createFeatherCost(rarity, featherCount, random);
      ItemStack result = QuestShopCompat.createCurrency(priceCount);
      MerchantOffer offer = new MerchantOffer(tradeData.cost(), result, this.getMaxUses(rarity), this.getExperience(rarity), 0.05F);
      ItemStack display = new ItemStack(BirdCameraItems.BIRD_FEATHER, featherCount);
      BirdFeatherItem.setFeatherData(display, tradeData.featherData());
      return MerchantOfferBuilder.of(offer).displayCost(display).displayResult(result).build();
   }

   private BirdSkinRarity selectValidRarity(int level, RandomSource random) {
      BirdSkinRarity rarity = FALLBACK_RARITY;

      for (int attempt = 0; attempt < 10; attempt++) {
         rarity = this.randomRarityByLevel(random, level);
         if (this.hasFeatherRarity(rarity)) {
            break;
         }

         if (attempt == 9) {
            rarity = FALLBACK_RARITY;
         }
      }

      return rarity;
   }

   private BirdFeatherTrade.FeatherTradeData createFeatherCost(BirdSkinRarity target, int count, RandomSource random) {
      List<BirdFeatherData> candidates = new ArrayList<>();

      for (Map.Entry<ResourceLocation, BirdData> holder : BirdCameraBirdData.VIEW.entrySet()) {
         if (this.holdsRarity(holder.getValue(), target)) {
            candidates.add(BirdFeatherData.create(holder.getKey(), target.getRarity()));
         }
      }

      if (candidates.isEmpty()) {
         BirdFeatherData fallback = BirdFeatherData.create(BirdCameraMod.id("budgerigar"), target.getRarity());
         DataComponentPredicate predicate = DataComponentPredicate.builder()
            .expect(BirdCameraDataComponents.BIRD_FEATHER_DATA, fallback)
            .build();
         return new BirdFeatherTrade.FeatherTradeData(this.featherItemCost(count, predicate), fallback);
      } else {
         BirdFeatherData data = candidates.get(random.nextInt(candidates.size()));
         DataComponentPredicate predicate = DataComponentPredicate.builder()
            .expect(BirdCameraDataComponents.BIRD_FEATHER_DATA, data)
            .build();
         return new BirdFeatherTrade.FeatherTradeData(this.featherItemCost(count, predicate), data);
      }
   }

   private boolean holdsRarity(BirdData data, BirdSkinRarity rarity) {
      if (data.model() == null || data.model().birdSkin() == null) {
         return false;
      }
      return data.model().birdSkin().stream()
         .anyMatch(s -> s.rarity() != null && s.rarity().getTranslationKey().equals(rarity.getTranslationKey()));
   }

   private ItemCost featherItemCost(int count, DataComponentPredicate predicate) {
      return new ItemCost(BuiltInRegistries.ITEM.wrapAsHolder(BirdCameraItems.BIRD_FEATHER), count, predicate);
   }

   private boolean hasFeatherRarity(BirdSkinRarity rarity) {
      for (BirdData entry : BirdCameraBirdData.VIEW.values()) {
         if (this.holdsRarity(entry, rarity)) {
            return true;
         }
      }

      return false;
   }

   private int getFeatherCountForTrade(BirdSkinRarity rarity) {
      return switch (rarity) {
         case COMMON -> 16;
         case UNCOMMON -> 8;
         case RARE -> 4;
         case EPIC -> 2;
         default -> 1;
      };
   }

   private int getPrice(BirdSkinRarity rarity) {
      return switch (rarity) {
         case COMMON, UNCOMMON, RARE, EPIC, LEGENDARY -> 1;
         case ANCIENT -> 2;
         case HIDDEN -> 3;
         case UNIQUE -> 4;
      };
   }

   private int getExperience(BirdSkinRarity rarity) {
      return switch (rarity) {
         case COMMON -> 4;
         case UNCOMMON -> 6;
         case RARE -> 8;
         case EPIC -> 10;
         case LEGENDARY -> 12;
         case ANCIENT -> 14;
         case HIDDEN -> 16;
         case UNIQUE -> 18;
      };
   }

   private int getMaxUses(BirdSkinRarity rarity) {
      return switch (rarity) {
         case COMMON, UNCOMMON -> 32;
         case RARE, EPIC -> 16;
         default -> 8;
      };
   }

   private int getVillagerLevel(Entity trader) {
      return trader instanceof Villager villager ? villager.getVillagerData().getLevel() : 1;
   }

   private BirdSkinRarity randomRarityByLevel(RandomSource random, int level) {
      return switch (level) {
         case 1 -> random.nextInt(100) < 80 ? BirdSkinRarity.COMMON : BirdSkinRarity.UNCOMMON;
         case 2 -> random.nextInt(100) < 80 ? BirdSkinRarity.UNCOMMON : BirdSkinRarity.RARE;
         case 3 -> random.nextInt(100) < 80 ? BirdSkinRarity.RARE : BirdSkinRarity.EPIC;
         case 4 -> random.nextInt(100) < 80 ? BirdSkinRarity.EPIC : BirdSkinRarity.LEGENDARY;
         default -> {
            int r = random.nextInt(100);
            yield r < 40 ? BirdSkinRarity.LEGENDARY : (r < 60 ? BirdSkinRarity.ANCIENT : (r < 80 ? BirdSkinRarity.UNIQUE : BirdSkinRarity.HIDDEN));
         }
      };
   }

   private static record FeatherTradeData(ItemCost cost, BirdFeatherData featherData) {
   }
}