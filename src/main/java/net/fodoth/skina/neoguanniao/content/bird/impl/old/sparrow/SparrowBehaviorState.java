package net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow;

/**
 * 麻雀行为状态枚举
 * 定义麻雀在不同情境下的行为状态
 */
public enum SparrowBehaviorState {
    /** 空闲 */
    IDLE(false),
    /** 环顾四周 */
    LOOK_AROUND(false),
    /** 啄食 */
    PECKING(false),
    /** 觅食 */
    FORAGING(false),
    /** 警觉 */
    ALERT(false),
    /** 逃离 */
    FLEEING(false),
    /** 短途飞行 */
    SHORT_FLIGHT(true),
    /** 栖息 */
    PERCHING(false),
    /** 栖息（睡觉） */
    ROOSTING(false),
    /** 跟随主人 */
    FOLLOWING_OWNER(false);

    private final boolean airborne;

    SparrowBehaviorState(boolean airborne) {
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
        return this == FLEEING || this == SHORT_FLIGHT;
    }

    /**
     * 是否为飞行状态
     */
    public boolean isFlight() {
        return this == SHORT_FLIGHT;
    }

    /**
     * 是否为地面活动状态
     */
    public boolean isGroundActivity() {
        return this == FORAGING || this == PECKING || this == LOOK_AROUND || this == PERCHING;
    }

    /**
     * 是否为静止状态
     */
    public boolean isStationary() {
        return this == IDLE || this == LOOK_AROUND || this == PECKING
                || this == PERCHING || this == ROOSTING || this == ALERT;
    }
}