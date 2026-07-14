package net.fodoth.skina.neoguanniao.content.bird.impl.nightheron;

import net.minecraft.util.RandomSource;
import software.bernie.geckolib.animation.RawAnimation;

public enum NightHeronIdleAnimationChoice {
    BASE(NightHeronEntity.IDLE_ANIMATION, 70, 130),
    LONG_NECK_1(NightHeronEntity.IDLE_DIFF_1_ANIMATION, 62, 72),
    LONG_NECK_2(NightHeronEntity.IDLE_DIFF_2_ANIMATION, 62, 74),
    LONG_NECK_3(NightHeronEntity.IDLE_DIFF_3_ANIMATION, 62, 74),
    SCRATCH(NightHeronEntity.IDLE_DIFF_4_ANIMATION, 78, 90),
    LONG_NECK_5(NightHeronEntity.IDLE_DIFF_5_ANIMATION, 62, 78);

    public final RawAnimation animation;
    private final int minDuration;
    private final int maxDuration;

    NightHeronIdleAnimationChoice(RawAnimation animation, int minDuration, int maxDuration) {
        this.animation = animation;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int nextDuration(RandomSource random) {
        return this.minDuration + random.nextInt(this.maxDuration - this.minDuration + 1);
    }
}