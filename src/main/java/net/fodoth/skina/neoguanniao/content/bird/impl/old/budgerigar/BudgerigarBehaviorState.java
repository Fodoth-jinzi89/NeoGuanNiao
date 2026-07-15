package net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar;

public enum BudgerigarBehaviorState {
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
}