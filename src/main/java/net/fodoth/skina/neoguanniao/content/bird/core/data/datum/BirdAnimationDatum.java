package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Map;

// ============ 动画数据 ============
public record BirdAnimationDatum(
        ResourceLocation animationId,
        Map<String, RawAnimation> animationMap,
        int preenDuration,
        int preenDurationVariance,
        int idleDuration,
        int idleDurationVariance,
        int otherDuration,
        int otherDurationVariance,
        int trustTickerMaxLimit,
        int trustTickerLimit,
        int maxCuriousAndTrustingIndex,
        int minCuriousAndTrustingIndex,
        float mainIdleAnimationChance
) {

    public BirdAnimationDatum {
        animationMap = Map.copyOf(animationMap);
    }

    public static BirdAnimationDatum createDefault() {
        return new BirdAnimationDatum(
                null,
                Map.of(),
                45,
                45,
                55,
                70,
                35,
                35,
                800,
                400,
                9,
                5,
                0.9F
        );
    }

    public static BirdAnimationDatum withAnimationIdAndMap(ResourceLocation id, Map<String, RawAnimation> animationMap) {
        return new BirdAnimationDatum(
                id,
                animationMap,
                45,
                45,
                55,
                70,
                35,
                35,
                800,
                400,
                9,
                5,
                0.9F);
    }

    public BirdAnimationDatum withCuriousAndTrustingIndexRange(int maxCuriousAndTrustingIndex, int minCuriousAndTrustingIndex) {
        return new BirdAnimationDatum(
                animationId,
                animationMap,
                preenDuration,
                preenDurationVariance,
                idleDuration,
                idleDurationVariance,
                otherDuration,
                otherDurationVariance,
                trustTickerMaxLimit,
                trustTickerLimit,
                maxCuriousAndTrustingIndex,
                minCuriousAndTrustingIndex,
                mainIdleAnimationChance
        );
    }

    public BirdAnimationDatum withMainIdleAnimationChance(int mainIdleAnimationChance) {
        return new BirdAnimationDatum(
                animationId,
                animationMap,
                preenDuration,
                preenDurationVariance,
                idleDuration,
                idleDurationVariance,
                otherDuration,
                otherDurationVariance,
                trustTickerMaxLimit,
                trustTickerLimit,
                maxCuriousAndTrustingIndex,
                minCuriousAndTrustingIndex,
                mainIdleAnimationChance
        );
    }
}