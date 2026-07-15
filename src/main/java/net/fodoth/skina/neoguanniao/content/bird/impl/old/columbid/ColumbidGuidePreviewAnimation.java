package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid;


import software.bernie.geckolib.animation.RawAnimation;

public enum ColumbidGuidePreviewAnimation {
    NONE(null),
    IDLE(AbstractColumbidEntity.IDLE_ANIMATION),
    LOOK_1(AbstractColumbidEntity.IDLE_DIFF_1_ANIMATION),
    LOOK_2(AbstractColumbidEntity.IDLE_DIFF_2_ANIMATION),
    LOOK_3(AbstractColumbidEntity.IDLE_DIFF_3_ANIMATION),
    WALK(AbstractColumbidEntity.WALK_ANIMATION),
    FLY_FLAP(AbstractColumbidEntity.FLY_FLAPPING_LOOP_ANIMATION),
    GLIDE(AbstractColumbidEntity.FLY_LOOP_ANIMATION);

    private final RawAnimation animation;

    ColumbidGuidePreviewAnimation(RawAnimation animation) {
        this.animation = animation;
    }

    public RawAnimation animation() {
        return this.animation;
    }
}