package net.fodoth.skina.neoguanniao.content.bird.columbid;

/**
 * 鸽子定义类
 * 包含鸽子实体的所有常量定义
 */
public final class PigeonDefinition {

    // ============ 实体标识 ============
    public static final String ENTITY_ID = "pigeon";
    public static final String SPAWN_EGG_ID = "pigeon_spawn_egg";

    // ============ 刷怪蛋颜色 ============
    public static final int SPAWN_EGG_BASE_COLOR;
    public static final int SPAWN_EGG_SPOT_COLOR;

    // ============ 实体尺寸 ============
    public static final float WIDTH = 0.4F;
    public static final float HEIGHT = 0.54F;

    // ============ 基础属性 ============
    public static final double MAX_HEALTH = 8.0;
    public static final double WALK_SPEED = 0.22;
    public static final double FLYING_SPEED = 0.42;
    public static final double FOLLOW_RANGE = 18.0;

    private PigeonDefinition() {
    }

    static {
        SPAWN_EGG_BASE_COLOR = ColumbidVariant.GRAY_PIGEON.baseColor();
        SPAWN_EGG_SPOT_COLOR = ColumbidVariant.WHITE_PIGEON.baseColor();
    }
}