package net.fodoth.skina.neoguanniao.content.bird.impl.budgerigar;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

/**
 * 虎皮鹦鹉定义类
 * 包含虎皮鹦鹉实体的所有常量定义，包括尺寸、属性、纹理变体和模型资源
 */
public final class BudgerigarDefinition {

    // ============ 实体标识 ============
    public static final String ENTITY_ID = "budgerigar";
    public static final String SPAWN_EGG_ID = "budgerigar_spawn_egg";

    // ============ 刷怪蛋颜色 ============
    public static final int SPAWN_EGG_BASE_COLOR = 7323461;
    public static final int SPAWN_EGG_SPOT_COLOR = 16111690;

    // ============ 实体尺寸 ============
    public static final float WIDTH = 0.204F;
    public static final float HEIGHT = 0.252F;

    // ============ 基础属性 ============
    public static final double MAX_HEALTH = 6.0;
    public static final double WALK_SPEED = 0.24;
    public static final double FLYING_SPEED = 0.32;
    public static final double FOLLOW_RANGE = 18.0;

    // ============ 社交参数 ============
    public static final int SOCIAL_RADIUS = 14;
    public static final int MIN_FLOCK_SIZE = 3;
    public static final int MAX_FLOCK_SIZE = 10;

    // ============ 模型资源 ============
    public static final ResourceLocation MODEL = resource("geo/budgerigar.geo.json");
    public static final ResourceLocation TEXTURE = resource("textures/entity/budgerigar.png");
    public static final ResourceLocation ANIMATION = resource("animations/budgerigar.animation.json");

    // ============ 纹理变体 ============
    public static final ResourceLocation[] TEXTURE_VARIANTS;

    private BudgerigarDefinition() {
    }

    /**
     * 根据变体索引获取对应的纹理资源
     *
     * @param variant 变体索引
     * @return 纹理资源位置
     */
    public static ResourceLocation textureForVariant(int variant) {
        return variant >= 0 && variant < TEXTURE_VARIANTS.length
                ? TEXTURE_VARIANTS[variant]
                : TEXTURE;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }

    static {
        TEXTURE_VARIANTS = new ResourceLocation[]{
                TEXTURE,
                resource("textures/entity/budgerigar/white_lark.png"),
                resource("textures/entity/budgerigar/mystery_green.png"),
                resource("textures/entity/budgerigar/blue_lark.png"),
                resource("textures/entity/budgerigar/blue_porcelain.png"),
                resource("textures/entity/budgerigar/yellow_lark.png"),
                resource("textures/entity/budgerigar/yellow.png"),
                resource("textures/entity/budgerigar/yellow_2.png"),
                resource("textures/entity/budgerigar/yellow_black.png"),
                resource("textures/entity/budgerigar/black_white.png")
        };
    }
}