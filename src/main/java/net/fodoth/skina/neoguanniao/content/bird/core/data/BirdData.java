package net.fodoth.skina.neoguanniao.content.bird.core.data;

import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.*;

public record BirdData(
        BirdAnimationDatum animation,
        BirdEatingDatum eating,
        BirdFlyingDatum flying,
        BirdFrightDatum fright,
        BirdMiscDatum misc,
        BirdSkinDatum model,
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
                BirdSkinDatum.createDefault(),
                BirdSoundDatum.createDefault(),
                BirdTameDatum.createDefault()
        );
    }

    public BirdData withAnimation(BirdAnimationDatum animation) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withEating(BirdEatingDatum eating) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withFlying(BirdFlyingDatum flying) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withFright(BirdFrightDatum fright) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withMisc(BirdMiscDatum misc) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withModel(BirdSkinDatum model) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withSound(BirdSoundDatum sound) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }

    public BirdData withTame(BirdTameDatum tame) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame);
    }
}