package net.fodoth.skina.neoguanniao.content.bird.impl.budgerigar;


import software.bernie.geckolib.animation.RawAnimation;

public enum BudgerigarGuidePreviewAnimation {
    NONE(null),
    IDLE(BudgerigarEntity.IDLE_ANIMATION),
    PREEN(BudgerigarEntity.PREEN_ANIMATION),
    CURIOUS(BudgerigarEntity.CURIOUS_ANIMATION),
    DANCE(BudgerigarEntity.DANCE_ANIMATION),
    EAT(BudgerigarEntity.EAT_ANIMATION),
    SLEEP(BudgerigarEntity.SLEEP_ANIMATION),
    WALK(BudgerigarEntity.WALK_ANIMATION),
    FLY(BudgerigarEntity.FLY_ANIMATION);

    private final RawAnimation animation;

    BudgerigarGuidePreviewAnimation(RawAnimation animation) {
        this.animation = animation;
    }

    public RawAnimation animation() {
        return this.animation;
    }
}