package net.fodoth.skina.neoguanniao.content.bird.sparrow;

import net.minecraft.util.RandomSource;
import software.bernie.geckolib.animation.RawAnimation;

public enum SparrowIdleAnimationChoice {
    BASE(SparrowEntity.IDLE_ANIMATION, 55, 115),
    TAIL(SparrowEntity.TAIL_ANIMATION, 48, 78),
    PECK(SparrowEntity.PECK_ANIMATION, 34, 58),
    LOOK_AROUND(SparrowEntity.LOOK_AROUND_ANIMATION, 58, 88);

    public final RawAnimation animation;
    private final int minDuration;
    private final int maxDuration;

    SparrowIdleAnimationChoice(RawAnimation animation, int minDuration, int maxDuration) {
        this.animation = animation;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int nextDuration(RandomSource random) {
        return this.minDuration + random.nextInt(this.maxDuration - this.minDuration + 1);
    }
}