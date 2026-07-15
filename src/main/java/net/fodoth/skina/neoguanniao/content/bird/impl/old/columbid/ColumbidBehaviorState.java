package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid;

public enum ColumbidBehaviorState {
    IDLE,
    WALKING,
    FORAGING,
    FOLLOWING_OWNER,
    PAIR_FOLLOWING,
    CHASING,
    ALERT,
    FLEEING,
    FLAP_FLYING,
    GLIDING,
    ROOSTING,
    SLEEPING,
    EATING,
    PREENING,
    CURIOUS,
    COURTING;

    public boolean isAirborne() {
        return this == FLAP_FLYING || this == GLIDING || this == FLEEING;
    }

    public boolean isEscape() {
        return this == FLEEING || this == ALERT;
    }
}