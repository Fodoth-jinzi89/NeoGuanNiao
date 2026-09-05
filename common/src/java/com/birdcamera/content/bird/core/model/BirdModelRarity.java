package com.birdcamera.content.bird.core.model;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;

public enum BirdModelRarity {
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
   private static final Map<BirdModelRarity, Integer> DEFAULT_WEIGHTS = new EnumMap<>(BirdModelRarity.class);
   private static volatile Map<BirdModelRarity, Integer> customWeights = null;
   private static final Map<Integer, BirdModelRarity> VALUE_MAP = new HashMap<>();
   private static final Int2ObjectMap<BirdModelRarity> BY_ID;

   private BirdModelRarity(int rarity, ChatFormatting color) {
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

   public static BirdModelRarity fromValue(int value) {
      return VALUE_MAP.getOrDefault(value, COMMON);
   }

   public static BirdModelRarity fromValueStrict(int value) {
      BirdModelRarity rarity = VALUE_MAP.get(value);
      if (rarity == null) {
         throw new IllegalArgumentException("No BirdModelRarity found for value: " + value);
      } else {
         return rarity;
      }
   }

   public int getWeight() {
      Map<BirdModelRarity, Integer> weights = customWeights != null ? customWeights : DEFAULT_WEIGHTS;
      return weights.getOrDefault(this, 0);
   }

   public static void setCustomWeights(Map<BirdModelRarity, Integer> weights) {
      if (weights != null) {
         for (Entry<BirdModelRarity, Integer> entry : weights.entrySet()) {
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

   public static Map<BirdModelRarity, Integer> getCurrentWeights() {
      return Map.copyOf(customWeights != null ? customWeights : DEFAULT_WEIGHTS);
   }

   public static BirdModelRarity byRarity(int rarity) {
      return (BirdModelRarity)BY_ID.getOrDefault(rarity, COMMON);
   }

   public static Map<BirdModelRarity, Integer> getDefaultWeights() {
      return Map.copyOf(DEFAULT_WEIGHTS);
   }

   public String getTranslationKey() {
      return this.name().toLowerCase(Locale.ROOT);
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

      for (BirdModelRarity rarity : values()) {
         VALUE_MAP.put(rarity.RARITY, rarity);
      }

      BY_ID = new Int2ObjectOpenHashMap();

      for (BirdModelRarity rarity : values()) {
         BY_ID.put(rarity.getRarity(), rarity);
      }
   }
}
