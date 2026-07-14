package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdGuidePreviewAnimation;

// ============ 动画数据 ============
public record BirdAnimationDatum(
        BirdGuidePreviewAnimation guidePreviewAnimation
) {
    public static BirdAnimationDatum createDefault() {
        return new BirdAnimationDatum(BirdGuidePreviewAnimation.NONE);
    }
}