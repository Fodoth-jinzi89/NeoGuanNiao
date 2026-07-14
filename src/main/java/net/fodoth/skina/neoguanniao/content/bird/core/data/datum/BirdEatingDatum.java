package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

// ============ 进食数据 ============
public record BirdEatingDatum(
        int eatingTicks,
        int eatingTicksVariant,
        int foodTicks,
        int foodTicksVariant,
        float eatAmount,
        float eatSoundVolume,
        float eatSoundVolumeVariant,
        float eatSoundPitch,
        float eatSoundPitchVariant,
        float eatBathMultiplier,
        int eatingTicksLimitForBath,
        int curiousTicksLimitForDroppedFood
) {
    public static BirdEatingDatum createDefault() {
        return new BirdEatingDatum(
                35, 21, 90, 60,
                0.35F, 0.45F, 0.05F, 1.35F, 0.2F, 0.8F,
                120, 120
        );
    }
}