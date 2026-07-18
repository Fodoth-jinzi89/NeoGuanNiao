package net.fodoth.skina.neoguanniao.content.bird.core;

import java.util.EnumSet;

public enum BirdBehaviorState {
    IDLE,
    WALKING,
    FORAGING,
    FOLLOWING,
    SENTINEL,
    ALERT,
    FLEEING,
    FLYING,
    ROOSTING,
    SLEEPING,
    EATING,
    PREENING,
    CURIOUS,
    DANCING,
    PERCHING,
    BATHING,
    USING_BATH;

    public boolean isAirborne() {
        return this == FLYING || this == FLEEING;
    }

    public boolean isEscape() {
        return this == FLEEING || this == ALERT;
    }

    /**
     * 判断是否启用不安全飞行计时器
     */
    public boolean isUnsafeFlyTickerEnabled() {
        return EnumSet.of(
                BirdBehaviorState.IDLE,
                BirdBehaviorState.SENTINEL,
                BirdBehaviorState.ALERT,
                BirdBehaviorState.PREENING,
                BirdBehaviorState.CURIOUS,
                BirdBehaviorState.DANCING,
                BirdBehaviorState.ROOSTING,
                BirdBehaviorState.SLEEPING
        ).contains(this);
    }

    /**
     * 判断是否启用不安全浮动计时器
     */
    public boolean isUnsafeFloatTickerEnabled() {
        EnumSet<BirdBehaviorState> flyStates = EnumSet.of(
                BirdBehaviorState.IDLE,
                BirdBehaviorState.WALKING,
                BirdBehaviorState.SENTINEL,
                BirdBehaviorState.ALERT,
                BirdBehaviorState.PREENING,
                BirdBehaviorState.CURIOUS,
                BirdBehaviorState.DANCING,
                BirdBehaviorState.EATING
        );

        return flyStates.contains(this);
    }
}
