package net.fodoth.skina.neoguanniao.content.bird.nightheron;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

/**
 * 夜鹭定义类
 * 包含夜鹭实体的所有常量定义，包括尺寸、速度、行为参数等
 */
public final class NightHeronDefinition {

    // ============ 实体标识 ============
    public static final String ENTITY_ID = "night_heron";
    public static final String SPAWN_EGG_ID = "night_heron_spawn_egg";

    // ============ 刷怪蛋颜色 ============
    public static final int SPAWN_EGG_BASE_COLOR = 6121331;
    public static final int SPAWN_EGG_SPOT_COLOR = 14198125;

    // ============ 实体尺寸 ============
    public static final float WIDTH = 0.8F;
    public static final float HEIGHT = 1.9F;

    // ============ 基础属性 ============
    public static final double MAX_HEALTH = 14.0;
    public static final double WALK_SPEED = 0.2;
    public static final double FLY_SPEED = 0.45;
    public static final double FOLLOW_RANGE = 24.0;
    public static final double ATTACK_DAMAGE = 2.0;

    // ============ 感知距离 ============
    public static final double ALERT_DISTANCE = 13.0;
    public static final double WALK_ESCAPE_DISTANCE = 9.0;
    public static final double RUN_ESCAPE_DISTANCE = 6.0;
    public static final double PLAYER_FLIGHT_DISTANCE = 4.25;
    public static final double PLAYER_SPRINT_FLIGHT_DISTANCE = 8.0;
    public static final double LONG_ESCAPE_DISTANCE = 3.0;

    // ============ 逃离计时 ============
    public static final int ALERT_MIN_TICKS = 18;
    public static final int ALERT_MAX_TICKS = 36;
    public static final int GROUND_ESCAPE_MIN_TICKS = 30;
    public static final int GROUND_ESCAPE_MAX_TICKS = 58;
    public static final int FRIGHTENED_FLIGHT_MIN_TICKS = 40;
    public static final int FRIGHTENED_FLIGHT_MAX_TICKS = 72;
    public static final int LONG_ESCAPE_MIN_TICKS = 150;
    public static final int LONG_ESCAPE_MAX_TICKS = 260;
    public static final int FRIGHT_MEMORY_TICKS = 220;
    public static final int LONG_ESCAPE_MEMORY_THRESHOLD = 3;

    // ============ 逃离速度 ============
    public static final double WALK_ESCAPE_SPEED = 0.36;
    public static final double GROUND_ESCAPE_SPEED = 1.35;
    public static final double FLIGHT_ESCAPE_SPEED = 0.36;
    public static final double FLIGHT_BURST_SPEED = 0.48;
    public static final double LONG_ESCAPE_SPEED = 0.55;

    // ============ 起飞参数 ============
    public static final int TAKEOFF_FLAP_TICKS = 20;
    public static final double TAKEOFF_CLEARANCE_DISTANCE = 4.5;
    public static final double TAKEOFF_VERTICAL_BOOST = 0.72;

    // ============ 飞行参数 ============
    public static final double FLIGHT_CRUISE_LIFT = 0.11;
    public static final double FLIGHT_DESCEND_SPEED = -0.045;
    public static final double GLIDE_DESCENT_SPEED = -0.025;

    // ============ 快速扑翼 ============
    public static final int FLIGHT_FAST_FLAP_PERIOD_TICKS = 38;
    public static final int FLIGHT_FAST_FLAP_TICKS = 13;
    public static final double FLIGHT_FAST_FLAP_SPEED_MULTIPLIER = 1.18;
    public static final double FLIGHT_FAST_FLAP_LIFT = 0.075;

    // ============ 逃离高度 ============
    public static final double MIN_ESCAPE_HEIGHT = 4.0;
    public static final double MAX_ESCAPE_HEIGHT = 8.0;
    public static final double LONG_ESCAPE_TARGET_HEIGHT = 20.0;
    public static final double LONG_ESCAPE_MAX_HEIGHT = 32.0;
    public static final double GLIDE_MIN_HEIGHT = 12.0;
    public static final double GLIDE_MIN_HORIZONTAL_SPEED = 0.12;

    // ============ 高空转场 ============
    public static final int HIGH_TRANSIT_MIN_TICKS = 120;
    public static final int HIGH_TRANSIT_MAX_TICKS = 220;
    public static final double HIGH_TRANSIT_SPEED = 0.36;
    public static final double HIGH_TRANSIT_TARGET_HEIGHT = 18.0;
    public static final double HIGH_TRANSIT_MAX_HEIGHT = 28.0;

    // ============ 着陆参数 ============
    public static final double LANDING_SPEED = 0.24;
    public static final double LANDING_APPROACH_SPEED = 0.28;
    public static final double LANDING_FLARE_SPEED = 0.22;
    public static final double LANDING_FINAL_RADIUS = 1.15;
    public static final int LANDING_APPROACH_TICKS = 60;

    // ============ 飞行受阻恢复 ============
    public static final int BLOCKED_FLIGHT_RECOVERY_ACTIVITY_TICKS = 70;
    public static final int BLOCKED_FLIGHT_RECOVERY_DIRECTION_TICKS = 28;
    public static final double BLOCKED_FLIGHT_MIN_CLEAR_SCORE = 1.25;
    public static final double BLOCKED_FLIGHT_RECOVERY_SPEED = 0.18;

    // ============ 着陆接近距离 ============
    public static final double LOCAL_LANDING_APPROACH_DISTANCE = 14.0;
    public static final double HIGH_LANDING_APPROACH_DISTANCE = 26.0;
    public static final double LONG_LANDING_APPROACH_DISTANCE = 34.0;

    // ============ 环境飞行 ============
    public static final int AMBIENT_FLIGHT_ACTIVE_CHANCE = 260;
    public static final int AMBIENT_FLIGHT_ROOST_CHANCE = 720;
    public static final int AMBIENT_FLIGHT_MIN_TICKS = 80;
    public static final int AMBIENT_FLIGHT_MAX_TICKS = 170;
    public static final int SOARING_FLIGHT_MIN_TICKS = 170;
    public static final int SOARING_FLIGHT_MAX_TICKS = 320;

    // ============ 飞行速度 ============
    public static final double LOCAL_FLIGHT_SPEED = 0.29;
    public static final double LOCAL_FLIGHT_TARGET_HEIGHT = 7.0;
    public static final double LOCAL_FLIGHT_MAX_HEIGHT = 13.0;
    public static final double SOARING_FLIGHT_SPEED = 0.34;
    public static final double SOARING_TARGET_HEIGHT = 23.0;
    public static final double SOARING_MAX_HEIGHT = 36.0;

    // ============ 空闲行为 ============
    public static final int IDLE_MIN_TICKS = 50;
    public static final int IDLE_MAX_TICKS = 140;
    public static final int MICRO_STROLL_MIN_TICKS = 35;
    public static final int MICRO_STROLL_MAX_TICKS = 80;
    public static final double IDLE_STROLL_SPEED = 0.16;

    // ============ 觅食 ============
    public static final double FORAGE_STROLL_SPEED = 0.14;
    public static final int FORAGE_MIN_TICKS = 120;
    public static final int FORAGE_MAX_TICKS = 260;
    public static final double FORAGE_PREY_RADIUS = 7.0;
    public static final double FORAGE_ATTACK_DISTANCE = 2.1;
    public static final int FORAGE_ATTACK_COOLDOWN = 45;

    // ============ 社交 ============
    public static final double SOCIAL_RADIUS = 8.0;
    public static final double SOCIAL_MIN_DISTANCE = 2.25;
    public static final double SOCIAL_COMFORT_DISTANCE = 5.0;
    public static final double SOCIAL_SPEED = 0.17;

    // ============ 栖息 ============
    public static final int ROOST_FLIGHT_NEAR_CHANCE = 120;
    public static final int ROOST_FLIGHT_SETTLED_CHANCE = 650;
    public static final int ROOST_GROUP_RADIUS = 12;
    public static final int ROOST_COVER_RADIUS = 5;

    // ============ 调试 ============
    public static final boolean DEBUG_BRAIN = false;
    public static final int DEBUG_BRAIN_LOG_INTERVAL_TICKS = 40;

    // ============ 大脑参数 ============
    public static final float BRAIN_EAT_HUNGER_REDUCTION = 0.45F;
    public static final float FORAGING_STOP_FEAR_THRESHOLD = 0.7F;
    public static final float FORAGING_STOP_RISK_THRESHOLD = 0.75F;

    // ============ 风险阈值 ============
    public static final float RISK_NONE_THRESHOLD = 0.25F;
    public static final float RISK_ALERT_THRESHOLD = 0.45F;
    public static final float RISK_WALK_THRESHOLD = 0.62F;
    public static final float RISK_RUN_THRESHOLD = 0.78F;
    public static final float RISK_LOW_FLIGHT_THRESHOLD = 0.88F;

    // ============ 近期恐惧 ============
    public static final float RECENT_FRIGHT_RISK_BONUS_MAX = 0.12F;
    public static final float RECENT_FRIGHT_RISK_BONUS_PER_COUNT = 0.04F;
    public static final int RECENT_FRIGHT_LONG_FLIGHT_COUNT = 2;

    // ============ 恐惧量 ============
    public static final float FRIGHT_AMOUNT_ALERT = 0.08F;
    public static final float FRIGHT_AMOUNT_WALK = 0.1F;
    public static final float FRIGHT_AMOUNT_RUN = 0.16F;
    public static final float FRIGHT_AMOUNT_LOW_FLIGHT = 0.24F;
    public static final float FRIGHT_AMOUNT_LONG_FLIGHT = 0.35F;
    public static final float LONG_FLIGHT_FATIGUE_LIMIT = 0.75F;

    // ============ 空闲行为权重 ============
    public static final float IDLE_COMFORT_HIGH = 0.65F;
    public static final float IDLE_ALERTNESS_HIGH = 0.45F;
    public static final float IDLE_FATIGUE_HIGH = 0.6F;
    public static final float IDLE_HUNGER_HIGH = 0.55F;
    public static final float IDLE_FEAR_HIGH = 0.55F;

    // ============ 空闲行为基础权重 ============
    public static final int IDLE_WEIGHT_STAND_BASE = 30;
    public static final int IDLE_WEIGHT_LOOK_BASE = 18;
    public static final int IDLE_WEIGHT_STROLL_BASE = 14;
    public static final int IDLE_WEIGHT_PREEN_BASE = 10;
    public static final int IDLE_WEIGHT_NECK_STRETCH_BASE = 10;

    // ============ 空闲行为 - 栖息加成 ============
    public static final int IDLE_ROOST_STAND_BONUS = 28;
    public static final int IDLE_ROOST_LOOK_BONUS = 8;
    public static final int IDLE_ROOST_STROLL_PENALTY = 10;
    public static final int IDLE_ROOST_PREEN_BONUS = 8;
    public static final int IDLE_ROOST_NECK_STRETCH_BONUS = 4;

    // ============ 空闲行为 - 舒适加成 ============
    public static final int IDLE_COMFORT_STAND_BONUS = 12;
    public static final int IDLE_COMFORT_PREEN_BONUS = 18;

    // ============ 空闲行为 - 警觉加成 ============
    public static final int IDLE_ALERT_LOOK_BONUS = 24;
    public static final int IDLE_ALERT_NECK_STRETCH_BONUS = 22;
    public static final int IDLE_ALERT_PREEN_PENALTY = 6;
    public static final int IDLE_ALERT_STROLL_PENALTY = 4;

    // ============ 空闲行为 - 疲劳加成 ============
    public static final int IDLE_FATIGUE_STAND_BONUS = 25;
    public static final int IDLE_FATIGUE_STROLL_PENALTY = 10;
    public static final int IDLE_FATIGUE_PREEN_BONUS = 5;

    // ============ 空闲行为 - 饥饿加成 ============
    public static final int IDLE_HUNGER_LOOK_BONUS = 18;
    public static final int IDLE_HUNGER_NECK_STRETCH_BONUS = 10;

    // ============ 空闲行为 - 恐惧加成 ============
    public static final int IDLE_FEAR_PREEN_PENALTY = 10;
    public static final int IDLE_FEAR_STROLL_PENALTY = 8;
    public static final int IDLE_FEAR_LOOK_BONUS = 12;
    public static final int IDLE_FEAR_NECK_STRETCH_BONUS = 12;

    // ============ 模型资源 ============
    public static final ResourceLocation MODEL = resource("geo/night_heron.geo.json");
    public static final ResourceLocation TEXTURE = resource("textures/entity/night_heron.png");
    public static final ResourceLocation ANIMATION = resource("animations/night_heron.animation.json");

    private NightHeronDefinition() {
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }
}