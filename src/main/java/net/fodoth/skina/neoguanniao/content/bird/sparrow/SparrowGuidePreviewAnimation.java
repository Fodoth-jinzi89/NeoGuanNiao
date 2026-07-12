package net.fodoth.skina.neoguanniao.content.bird.sparrow;

import software.bernie.geckolib.animation.RawAnimation;

public enum SparrowGuidePreviewAnimation {
    NONE(null),
    IDLE(SparrowEntity.IDLE_ANIMATION),
    TAIL(SparrowEntity.TAIL_ANIMATION),
    PECK(SparrowEntity.PECK_ANIMATION),
    LOOK_AROUND(SparrowEntity.LOOK_AROUND_ANIMATION),
    WALK(SparrowEntity.WALK_ANIMATION),
    FLY(SparrowEntity.FLY_ANIMATION);

    private final RawAnimation animation;

    SparrowGuidePreviewAnimation(RawAnimation animation) {
        this.animation = animation;
    }

    public RawAnimation animation() {
        return this.animation;
    }
}