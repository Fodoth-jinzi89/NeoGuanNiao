package net.fodoth.skina.neoguanniao.content.bird.core.data;

import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.*;

public record BirdData(
        BirdAnimationDatum animation,
        BirdEatingDatum eating,
        BirdFlyingDatum flying,
        BirdFrightDatum fright,
        BirdMiscDatum misc,
        BirdModelSkinDatum model,
        BirdSoundDatum sound,
        BirdTameDatum tame,
        BirdGoalDatum goal
) {
    public static BirdData createDefault() {
        return new BirdData(
                BirdAnimationDatum.createDefault(),
                BirdEatingDatum.createDefault(),
                BirdFlyingDatum.createDefault(),
                BirdFrightDatum.createDefault(),
                BirdMiscDatum.createDefault(),
                BirdModelSkinDatum.createDefault(),
                BirdSoundDatum.createDefault(),
                BirdTameDatum.createDefault(),
                BirdGoalDatum.createDefault()
        );
    }

    public BirdData withAnimation(BirdAnimationDatum animation) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withEating(BirdEatingDatum eating) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withFlying(BirdFlyingDatum flying) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withFright(BirdFrightDatum fright) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withMisc(BirdMiscDatum misc) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withModel(BirdModelSkinDatum model) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withSound(BirdSoundDatum sound) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withTame(BirdTameDatum tame) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }

    public BirdData withGoal(BirdGoalDatum goal) {
        return new BirdData(animation, eating, flying, fright, misc, model, sound, tame, goal);
    }
}