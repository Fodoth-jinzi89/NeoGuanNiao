package net.fodoth.skina.neoguanniao.content.bird.core;

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
    PERCHING;

    public boolean isAirborne() {
        return this == FLYING || this == FLEEING;
    }

    public boolean isEscape() {
        return this == FLEEING || this == ALERT;
    }

    public boolean isActive() {
        return this != SLEEPING && this != ROOSTING;
    }

    public boolean isUnsafeFlyEnabled() {
        return this == BirdBehaviorState.IDLE || this == BirdBehaviorState.WALKING || this == BirdBehaviorState.SENTINEL || this == BirdBehaviorState.ALERT || this == BirdBehaviorState.ROOSTING || this == BirdBehaviorState.SLEEPING || this == BirdBehaviorState.PREENING || this == BirdBehaviorState.CURIOUS || this == BirdBehaviorState.DANCING;
    }

    public boolean isUnsafeFloatEnabled() {
        return isUnsafeFlyEnabled() || this == BirdBehaviorState.EATING;
    }
}
