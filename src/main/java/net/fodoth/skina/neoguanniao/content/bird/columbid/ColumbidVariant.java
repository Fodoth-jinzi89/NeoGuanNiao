package net.fodoth.skina.neoguanniao.content.bird.columbid;

import net.minecraft.resources.ResourceLocation;

/**
 * 鸽形目鸟类变体枚举
 * 定义不同鸽形目鸟类的纹理变体和刷怪蛋颜色
 */
public enum ColumbidVariant {
    /** 斑鸠 */
    SPOTTED_DOVE("spotted_dove", "textures/entity/spotted_dove.png", 8216140, 14075060),
    /** 灰色鸽子 */
    GRAY_PIGEON("gray_pigeon", "textures/entity/pigeon_gray.png", 7172215, 3092792),
    /** 白色鸽子 */
    WHITE_PIGEON("white_pigeon", "textures/entity/pigeon_white.png", 15263197, 12894392);

    private final String id;
    private final ResourceLocation texture;
    private final int baseColor;
    private final int spotColor;

    ColumbidVariant(String id, String texturePath, int baseColor, int spotColor) {
        this.id = id;
        this.texture = ResourceLocation.fromNamespaceAndPath("neoguanniao", texturePath);
        this.baseColor = baseColor;
        this.spotColor = spotColor;
    }

    /**
     * 获取变体 ID
     */
    public String id() {
        return this.id;
    }

    /**
     * 获取纹理资源位置
     */
    public ResourceLocation texture() {
        return this.texture;
    }

    /**
     * 获取刷怪蛋基础颜色
     */
    public int baseColor() {
        return this.baseColor;
    }

    /**
     * 获取刷怪蛋斑点颜色
     */
    public int spotColor() {
        return this.spotColor;
    }

    /**
     * 根据序号获取变体，如果无效则返回默认值
     *
     * @param ordinal 序号
     * @param fallback 默认值
     * @return 对应的变体
     */
    public static ColumbidVariant byOrdinal(int ordinal, ColumbidVariant fallback) {
        ColumbidVariant[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    /**
     * 根据序号获取鸽子变体（仅限于灰色或白色鸽子）
     *
     * @param ordinal 序号
     * @return 鸽子变体（灰色或白色）
     */
    public static ColumbidVariant pigeonByOrdinal(int ordinal) {
        ColumbidVariant variant = byOrdinal(ordinal, GRAY_PIGEON);
        return variant == WHITE_PIGEON ? WHITE_PIGEON : GRAY_PIGEON;
    }
}