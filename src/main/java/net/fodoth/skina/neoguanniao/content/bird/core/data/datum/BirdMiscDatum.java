package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

// ============ 杂项数据 ============
public record BirdMiscDatum(
        int spawnRarity,
        double walkingSpeedThreshold,
        double followingDistanceThreshold,
        long activeStartTime,
        long activeEndTime,
        int maxTurns,
        float mutantChance,
        int curiousTicksLimitForBath,
        int curiousTicksLimitForSharedTrust,
        int curiousTicksLimitForAlert,
        int curiousTicksLimitForTame,
        double trustShareRange,
        float droppedItemTrustMultiplier,
        int escapeCooldownMin,
        int escapeCooldownVariance,
        int tameCooldownMin,
        int tameCooldownVariance,
        int wildCooldownMin,
        int wildCooldownVariance,
        int postFlightAlertTicks,
        double alertNearbyRange,
        int alertTicks,
        int alertTicksVariant,
        int alertTicksPlayer,
        int alertTicksOther,
        float frightenedTrustLossPlayer,
        float frightenedTrustLossOther
) {
    public static BirdMiscDatum createDefault() {
        return new BirdMiscDatum(
                8,
                0.0025D,
                9.0D, 23000L, 11500L, 10, 0.05F,
                120, 120, 60, 60,
                10.0D, 0.5F,
                160, 160, 180, 28, 55, 45,
                50,
                14.0D, 24, 45, 35, 55,
                0.08F, 0.18F
        );
    }
}