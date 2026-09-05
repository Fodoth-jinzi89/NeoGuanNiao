package com.birdcamera.content.bird.core.skin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;

public enum BirdSkinRarity {
   COMMON(0, ChatFormatting.GRAY),
   UNCOMMON(1, ChatFormatting.GREEN),
   RARE(2, ChatFormatting.BLUE),
   EPIC(3, ChatFormatting.DARK_PURPLE),
   LEGENDARY(4, ChatFormatting.GOLD),
   ANCIENT(5, ChatFormatting.RED),
   UNIQUE(6, ChatFormatting.LIGHT_PURPLE),
   HIDDEN(999, ChatFormatting.YELLOW);

   private final int RARITY;
   private final ChatFormatting COLOR;
   private static final Map<BirdSkinRarity, Integer> DEFAULT_WEIGHTS = new EnumMap<>(BirdSkinRarity.class);
   private static volatile Map<BirdSkinRarity, Integer> customWeights = null;
   private static final Map<Integer, BirdSkinRarity> VALUE_MAP = new HashMap<>();
   private static final Int2ObjectMap<BirdSkinRarity> BY_ID;

   private BirdSkinRarity(int rarity, ChatFormatting color) {
      this.RARITY = rarity;
      this.COLOR = color;
   }

   public ChatFormatting getChatColor() {
      return this.COLOR;
   }

   public int getRarity() {
      return this.RARITY;
   }

   public boolean isCommon() {
      return this == COMMON;
   }

   public boolean isUnique() {
      return this == UNIQUE;
   }

   public boolean isHidden() {
      return this == HIDDEN;
   }

   public static BirdSkinRarity fromValue(int value) {
      return VALUE_MAP.getOrDefault(value, COMMON);
   }

   public static BirdSkinRarity fromValueStrict(int value) {
      BirdSkinRarity rarity = VALUE_MAP.get(value);
      if (rarity == null) {
         throw new IllegalArgumentException("No BirdSkinRarity found for value: " + value);
      } else {
         return rarity;
      }
   }

   public int getWeight() {
      Map<BirdSkinRarity, Integer> weights = customWeights != null ? customWeights : DEFAULT_WEIGHTS;
      return weights.getOrDefault(this, 0);
   }

   public static void setCustomWeights(Map<BirdSkinRarity, Integer> weights) {
      if (weights != null) {
         for (Entry<BirdSkinRarity, Integer> entry : weights.entrySet()) {
            if (entry.getValue() < 0) {
               throw new IllegalArgumentException("Weight cannot be negative for " + entry.getKey());
            }
         }
      }

      customWeights = weights;
   }

   public static void resetToDefaultWeights() {
      customWeights = null;
   }

   public static Map<BirdSkinRarity, Integer> getCurrentWeights() {
      return Map.copyOf(customWeights != null ? customWeights : DEFAULT_WEIGHTS);
   }

   public static Map<BirdSkinRarity, Integer> getDefaultWeights() {
      return Map.copyOf(DEFAULT_WEIGHTS);
   }

   public static BirdSkinRarity byRarity(int rarity) {
      return (BirdSkinRarity)BY_ID.getOrDefault(rarity, COMMON);
   }

   public String getTranslationKey() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public static BirdSkinRarity getNextRarityBeforeAncient(BirdSkinRarity rarity) {
      return switch (rarity) {
         case COMMON -> UNCOMMON;
         case UNCOMMON -> RARE;
         case RARE -> EPIC;
         case EPIC -> LEGENDARY;
         case LEGENDARY -> ANCIENT;
         default -> null;
      };
   }

   public static BirdSkinRarity getPreviousRarityBeforeAncient(BirdSkinRarity rarity) {
      return switch (rarity) {
         case UNCOMMON -> COMMON;
         case RARE -> UNCOMMON;
         case EPIC -> RARE;
         case LEGENDARY -> EPIC;
         case ANCIENT -> LEGENDARY;
         default -> null;
      };
   }

   static {
      DEFAULT_WEIGHTS.put(COMMON, 1000);
      DEFAULT_WEIGHTS.put(UNCOMMON, 400);
      DEFAULT_WEIGHTS.put(RARE, 100);
      DEFAULT_WEIGHTS.put(EPIC, 30);
      DEFAULT_WEIGHTS.put(LEGENDARY, 10);
      DEFAULT_WEIGHTS.put(ANCIENT, 1);
      DEFAULT_WEIGHTS.put(UNIQUE, 0);
      DEFAULT_WEIGHTS.put(HIDDEN, 0);

      for (BirdSkinRarity rarity : values()) {
         VALUE_MAP.put(rarity.RARITY, rarity);
      }

      BY_ID = new Int2ObjectOpenHashMap();

      for (BirdSkinRarity rarity : values()) {
         BY_ID.put(rarity.getRarity(), rarity);
      }
   }
}
