package net.fodoth.skina.neoguanniao.content.bird.columbid;

/**
 * 斑鸠定义类
 * 包含斑鸠实体的所有常量定义
 */
public final class SpottedDoveDefinition {

    // ============ 实体标识 ============
    public static final String ENTITY_ID = "spotted_dove";
    public static final String SPAWN_EGG_ID = "spotted_dove_spawn_egg";

    // ============ 刷怪蛋颜色 ============
    public static final int SPAWN_EGG_BASE_COLOR;
    public static final int SPAWN_EGG_SPOT_COLOR;

    // ============ 实体尺寸 ============
    public static final float WIDTH = 0.42F;
    public static final float HEIGHT = 0.58F;

    // ============ 基础属性 ============
    public static final double MAX_HEALTH = 8.0;
    public static final double WALK_SPEED = 0.22;
    public static final double FLYING_SPEED = 0.43;
    public static final double FOLLOW_RANGE = 20.0;

    private SpottedDoveDefinition() {
    }

    static {
        SPAWN_EGG_BASE_COLOR = ColumbidVariant.SPOTTED_DOVE.baseColor();
        SPAWN_EGG_SPOT_COLOR = ColumbidVariant.SPOTTED_DOVE.spotColor();
    }
}