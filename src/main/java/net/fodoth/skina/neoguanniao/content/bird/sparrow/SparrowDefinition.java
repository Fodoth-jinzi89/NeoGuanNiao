package net.fodoth.skina.neoguanniao.content.bird.sparrow;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

/**
 * 麻雀定义类
 * 包含麻雀实体的所有常量定义，包括尺寸、属性、模型资源等
 */
public final class SparrowDefinition {

    // ============ 实体标识 ============
    public static final String ENTITY_ID = "sparrow";
    public static final String SPAWN_EGG_ID = "sparrow_spawn_egg";

    // ============ 刷怪蛋颜色 ============
    public static final int SPAWN_EGG_BASE_COLOR = 9072205;
    public static final int SPAWN_EGG_SPOT_COLOR = 14141346;

    // ============ 实体尺寸 ============
    public static final float WIDTH = 0.32F;
    public static final float HEIGHT = 0.38F;

    // ============ 基础属性 ============
    public static final double MAX_HEALTH = 6.0;
    public static final double WALK_SPEED = 0.25;
    public static final double FOLLOW_RANGE = 18.0;

    // ============ 社交距离 ============
    public static final double SOCIAL_RADIUS = 9.0;

    // ============ 模型资源 ============
    public static final ResourceLocation MODEL = resource("geo/sparrow.geo.json");
    public static final ResourceLocation TEXTURE = resource("textures/entity/sparrow.png");
    public static final ResourceLocation ANIMATION = resource("animations/sparrow.animation.json");

    private SparrowDefinition() {
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }
}