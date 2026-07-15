package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron;

import software.bernie.geckolib.animation.RawAnimation;

public enum NightHeronGuidePreviewAnimation {
    NONE(null),
    IDLE(NightHeronEntity.IDLE_ANIMATION),
    LOOK_1(NightHeronEntity.IDLE_DIFF_1_ANIMATION),
    LOOK_2(NightHeronEntity.IDLE_DIFF_2_ANIMATION),
    LOOK_3(NightHeronEntity.IDLE_DIFF_3_ANIMATION),
    SCRATCH(NightHeronEntity.IDLE_DIFF_4_ANIMATION),
    LOOK_5(NightHeronEntity.IDLE_DIFF_5_ANIMATION),
    WALK(NightHeronEntity.WALK_ANIMATION),
    RUN(NightHeronEntity.RUN_ANIMATION),
    FLY_FLAP(NightHeronEntity.FLY_FLAPPING_WING_LOOP_ANIMATION),
    GLIDE(NightHeronEntity.FLY_LOOP_ANIMATION);

    private final RawAnimation animation;

    NightHeronGuidePreviewAnimation(RawAnimation animation) {
        this.animation = animation;
    }

    public RawAnimation animation() {
        return this.animation;
    }
}