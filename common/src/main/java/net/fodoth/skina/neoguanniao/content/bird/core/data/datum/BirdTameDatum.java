package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

// ============ 驯服行为数据 ============
public record BirdTameDatum(
        int trustTicksLimit,
        int addTrustValue,
        int addTrustNearbyValue,
        int trustTameThreshold,
        int tameCelebrationCuriousTicks,
        int tameCelebrationBehaviorStateTicks,
        int tameCelebrationPostTameActionTicksMin,
        int tameCelebrationPostTameActionTicksVariance,
        int tameCelebrationPostTameActionSwapTicks,
        int tameCelebrationFoodTicks,
        int tamedBehaviorTicks,
        int tamedBehaviorTicksVariance,
        int postTameActionSwapTicks,
        int postTameActionSwapTicksVariance
) {
    public static BirdTameDatum createDefault() {
        return new BirdTameDatum(
                6000, 420, 120, 900,
                220, 55, 150, 50, 42, 45,
                32, 32, 30, 28
        );
    }
}