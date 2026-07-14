package net.fodoth.skina.neoguanniao.content.bird.core.data;

import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.*;

public record BirdData(
        BirdAnimationDatum animation,
        BirdEatingDatum eating,
        BirdFlyingDatum flying,
        BirdFrightDatum fright,
        BirdMiscDatum misc,
        BirdModelDatum model,
        BirdSoundDatum sound,
        BirdTameDatum tame
) {
    public static BirdData createDefault() {
        return new BirdData(
                BirdAnimationDatum.createDefault(),
                BirdEatingDatum.createDefault(),
                BirdFlyingDatum.createDefault(),
                BirdFrightDatum.createDefault(),
                BirdMiscDatum.createDefault(),
                BirdModelDatum.createDefault(),
                BirdSoundDatum.createDefault(),
                BirdTameDatum.createDefault()
        );
    }
}