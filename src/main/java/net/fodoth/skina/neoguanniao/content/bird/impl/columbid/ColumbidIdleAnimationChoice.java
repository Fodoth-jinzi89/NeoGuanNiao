package net.fodoth.skina.neoguanniao.content.bird.impl.columbid;

import net.minecraft.util.RandomSource;
import software.bernie.geckolib.animation.RawAnimation;

public enum ColumbidIdleAnimationChoice {
    BASE(AbstractColumbidEntity.IDLE_ANIMATION, 70, 130),
    PECK_1(AbstractColumbidEntity.IDLE_DIFF_1_ANIMATION, 45, 70),
    PECK_2(AbstractColumbidEntity.IDLE_DIFF_2_ANIMATION, 48, 76),
    DISPLAY(AbstractColumbidEntity.IDLE_DIFF_3_ANIMATION, 70, 95);

    public final RawAnimation animation;
    private final int minDuration;
    private final int maxDuration;

    ColumbidIdleAnimationChoice(RawAnimation animation, int minDuration, int maxDuration) {
        this.animation = animation;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int nextDuration(RandomSource random) {
        return this.minDuration + random.nextInt(this.maxDuration - this.minDuration + 1);
    }
}