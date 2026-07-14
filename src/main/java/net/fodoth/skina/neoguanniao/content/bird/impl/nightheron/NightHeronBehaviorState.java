package net.fodoth.skina.neoguanniao.content.bird.impl.nightheron;

/**
 * 夜鹭行为状态枚举
 * 定义夜鹭在不同情境下的行为状态
 */
public enum NightHeronBehaviorState {
    /** 空闲 */
    IDLE(false),
    /** 休息站立 */
    REST_STAND(false),
    /** 环顾四周 */
    LOOK_AROUND(false),
    /** 微散步 */
    MICRO_STROLL(false),
    /** 整理羽毛 */
    PREEN(false),
    /** 伸脖子 */
    NECK_STRETCH(false),
    /** 水边等待 */
    WATER_EDGE_WAIT(false),
    /** 警觉冻结 */
    ALERT_FREEZE(false),
    /** 行走逃离 */
    WALK_ESCAPE(false),
    /** 奔跑逃离 */
    RUN_ESCAPE(false),
    /** 觅食 */
    FORAGING(false),
    /** 进食 */
    EATING(false),
    /** 栖息 */
    ROOSTING(false),
    /** 社交距离 */
    SOCIAL_SPACING(false),
    /** 起飞 */
    TAKEOFF(true),
    /** 本地飞行 */
    LOCAL_FLIGHT(true),
    /** 低空扑翼逃离 */
    LOW_FLAP_ESCAPE(true),
    /** 长距离飞行逃离 */
    LONG_FLIGHT_ESCAPE(true),
    /** 爬升 */
    CLIMB(true),
    /** 高空转场 */
    HIGH_TRANSIT(true),
    /** 翱翔 */
    SOARING(true),
    /** 滑翔 */
    GLIDE(true),
    /** 着陆 */
    LANDING(true);

    private final boolean airborne;

    NightHeronBehaviorState(boolean airborne) {
        this.airborne = airborne;
    }

    /**
     * 是否在空中
     */
    public boolean isAirborne() {
        return this.airborne;
    }

    /**
     * 是否为逃离状态
     */
    public boolean isEscape() {
        return this == WALK_ESCAPE || this == RUN_ESCAPE
                || this == LOW_FLAP_ESCAPE || this == LONG_FLIGHT_ESCAPE;
    }

    /**
     * 是否为飞行状态
     */
    public boolean isFlight() {
        return this == TAKEOFF || this == LOCAL_FLIGHT || this == LOW_FLAP_ESCAPE
                || this == LONG_FLIGHT_ESCAPE || this == CLIMB || this == HIGH_TRANSIT
                || this == SOARING || this == GLIDE || this == LANDING;
    }

    /**
     * 是否为地面移动状态
     */
    public boolean isGroundMovement() {
        return this == MICRO_STROLL || this == WALK_ESCAPE || this == RUN_ESCAPE;
    }

    /**
     * 是否为静止状态
     */
    public boolean isStationary() {
        return this == IDLE || this == REST_STAND || this == LOOK_AROUND
                || this == PREEN || this == NECK_STRETCH || this == WATER_EDGE_WAIT
                || this == ALERT_FREEZE || this == FORAGING || this == EATING
                || this == ROOSTING || this == SOCIAL_SPACING;
    }
}