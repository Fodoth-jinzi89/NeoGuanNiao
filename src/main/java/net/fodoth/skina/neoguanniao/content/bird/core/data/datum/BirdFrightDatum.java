package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

// ============ 惊吓数据 ============
public record BirdFrightDatum(
        int frightTicksLimit,
        float frightenAmount,
        int pendingFrightDurationLimit,
        int pendingFrightTicksLimit,
        int frightDelayMin,
        int frightDelayVariance,
        double pendingFrightLookYOffset,
        float pendingFrightLookSpeed,
        int pendingFrightMinDuration,
        int frightCheckTicks,
        int frightCheckTicksVariance,
        int frightenFromTicks,
        int frightenFromTicksVariance
) {
    public static BirdFrightDatum createDefault() {
        return new BirdFrightDatum(
                180, 0.06F, 24, 24, 18, 16,
                0.6F, 35.0F, 60,
                60,
                20,
                60,
                20
        );
    }
}