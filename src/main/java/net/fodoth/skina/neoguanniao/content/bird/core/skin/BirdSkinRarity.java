package net.fodoth.skina.neoguanniao.content.bird.core.skin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    // 默认权重Map
    private static final Map<BirdSkinRarity, Integer> DEFAULT_WEIGHTS = new EnumMap<>(BirdSkinRarity.class);

    // 可自定义的权重Map（线程安全，使用volatile保证可见性）
    private static volatile Map<BirdSkinRarity, Integer> customWeights = null;

    // 值到枚举的映射缓存 - 使用HashMap因为键是Integer不是Enum
    private static final Map<Integer, BirdSkinRarity> VALUE_MAP = new HashMap<>();

    static {
        // 初始化默认权重
        DEFAULT_WEIGHTS.put(COMMON, 1000);
        DEFAULT_WEIGHTS.put(UNCOMMON, 400);
        DEFAULT_WEIGHTS.put(RARE, 100);
        DEFAULT_WEIGHTS.put(EPIC, 30);
        DEFAULT_WEIGHTS.put(LEGENDARY, 10);
        DEFAULT_WEIGHTS.put(ANCIENT, 1);
        DEFAULT_WEIGHTS.put(UNIQUE, 0);
        DEFAULT_WEIGHTS.put(HIDDEN, 0);

        // 初始化值映射
        for (BirdSkinRarity rarity : values()) {
            VALUE_MAP.put(rarity.RARITY, rarity);
        }
    }

    BirdSkinRarity(int rarity, ChatFormatting color) {
        this.RARITY = rarity;
        this.COLOR = color;
    }


    public ChatFormatting getChatColor() {
        return COLOR;
    }


    public int getRarity() {
        return RARITY;
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

    /**
     * 根据数值获取对应的稀有度枚举
     *
     * @param value 稀有度数值
     * @return 对应的稀有度枚举，如果不存在则返回COMMON
     */
    public static BirdSkinRarity fromValue(int value) {
        return VALUE_MAP.getOrDefault(value, COMMON);
    }

    /**
     * 根据数值获取对应的稀有度枚举（严格模式）
     *
     * @param value 稀有度数值
     * @return 对应的稀有度枚举
     * @throws IllegalArgumentException 如果数值不存在
     */
    public static BirdSkinRarity fromValueStrict(int value) {
        BirdSkinRarity rarity = VALUE_MAP.get(value);
        if (rarity == null) {
            throw new IllegalArgumentException("No BirdSkinRarity found for value: " + value);
        }
        return rarity;
    }

    /**
     * 获取当前权重（优先使用自定义权重）
     */
    public int getWeight() {
        Map<BirdSkinRarity, Integer> weights = customWeights != null ? customWeights : DEFAULT_WEIGHTS;
        return weights.getOrDefault(this, 0);
    }

    /**
     * 设置自定义权重Map
     */
    public static void setCustomWeights(Map<BirdSkinRarity, Integer> weights) {
        // 验证权重值
        if (weights != null) {
            for (Map.Entry<BirdSkinRarity, Integer> entry : weights.entrySet()) {
                if (entry.getValue() < 0) {
                    throw new IllegalArgumentException("Weight cannot be negative for " + entry.getKey());
                }
            }
        }
        customWeights = weights;
    }

    /**
     * 重置为默认权重
     */
    public static void resetToDefaultWeights() {
        customWeights = null;
    }

    /**
     * 获取当前使用的权重Map（只读）
     */
    public static Map<BirdSkinRarity, Integer> getCurrentWeights() {
        return Map.copyOf(customWeights != null ? customWeights : DEFAULT_WEIGHTS);
    }

    /**
     * 获取默认权重Map（只读）
     */
    public static Map<BirdSkinRarity, Integer> getDefaultWeights() {
        return Map.copyOf(DEFAULT_WEIGHTS);
    }

    private static final Int2ObjectMap<BirdSkinRarity> BY_ID = new Int2ObjectOpenHashMap<>();

    static {
        for (BirdSkinRarity rarity : values()) {
            BY_ID.put(rarity.getRarity(), rarity);
        }
    }

    public static BirdSkinRarity byRarity(int rarity) {
        return BY_ID.getOrDefault(rarity, COMMON);
    }

    public String getTranslationKey() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}