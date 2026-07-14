package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;

// ============ 飞行数据 ============
public record BirdFlyingDatum(
        // 飞行基础
        BirdFlightProfile flightProfile,
        int minimumFlightTicks,
        int airborneGraceTicks,

        // 环境巡航
        int ambientAirCruiseMinTicks,
        int ambientAirCruiseRandomTicks,
        int escapeAirCruiseMinTicks,
        int escapeAirCruiseRandomTicks,

        // 飞越
        int minFlybyDuration,
        int flybyDurationVariance,
        double flybyHorizontalSpeed,
        double flybyUpwardSpeed,

        // 悬停
        int minHoverRetargetTicks,
        int hoverRetargetTicksVariance,
        int hoverRetargetMinDelay,
        int hoverRetargetDelayVariance,

        // 鸟浴台
        double birdBathMountHorizontalSpeed,
        double birdBathMountUpwardSpeed,
        int birdBathMountFlightTicks,

        // 起飞与逃离
        int shortFlyTicks,
        int shortFleeTicks,
        double escapeFlightMinDistance,
        double escapeFlightDistanceVariance,
        double escapeFlightMinHeight,
        double escapeFlightHeightVariance,

        // 飞行结束
        double flightLandingHorizontalDamping,

        // 降落
        int landingFlightMinDuration,
        int landingFlightDurationVariance,
        int landingFlightStateTicks,

        // 不安全降落
        int unsafeLandingCruiseMinDuration,
        int unsafeLandingCruiseDurationVariance,
        int unsafeLandingCruiseStateTicks,

        // 空中巡航目标
        int cruiseLookChanceDenominator,
        double cruiseFallbackHeightGround,
        double cruiseFallbackHeightAir,

        // 降落目标搜索
        int landingSharedRadius,
        int landingSharedVerticalRange,
        int landingSurfaceSearchRadius,
        int landingRandomAttempts,
        int landingRandomHorizontalRange,

        // 飞行高度限制
        double flightTargetMinHeightOffset,
        double flightTargetMaxHeightOffset,

        // 水上逃生
        int waterEscapeMinDuration,
        int waterEscapeRandomDuration,
        int waterEscapeBehaviorTicks,
        double waterEscapeHorizontalSpeed,
        double waterEscapeVerticalSpeed,
        int waterEscapeHoverRetargetMin,
        int waterEscapeHoverRetargetMax,

        // 飞行系统参数
        double flightTargetReachDistance,
        double flightLandingReachDistance,
        double flightMovementScale,
        double flightDesiredScale,
        double flightStalledThreshold,
        double flightStalledMinSpeed,
        double flightStalledVerticalBoost,
        double flightEscapeSpeed,
        double flightLandingSpeed,
        double flightAmbientSpeed,
        double flightLandingDecalDistance,
        double flightLandingDecalFactor,
        double flightHoverBobAmplitude,
        double flightHoverBobFrequency,
        double flightLandingHoverBob,
        double flightVerticalClampMin,
        double flightVerticalClampMax,
        double flightVerticalAmbientMin,
        double flightVerticalAmbientMax,
        double flightVerticalLandingFactor,
        double flightVerticalAmbientFactor,

        // 群体飞行
        double flockRange,
        double flockSeparation,
        double flockAlignment,
        double flockCohesion,
        double flockEscapeWeight,
        double flockAmbientWeight,
        double flockWeightEscape,

        // 巡航几率
        int ambientAirCruiseChanceTame,
        int ambientAirCruiseChanceWild
) {
    public static BirdFlyingDatum createDefault() {
        return new BirdFlyingDatum(
                // 飞行基础 (3)
                null, 90, 0,
                // 环境巡航 (4)
                110, 120, 80, 70,
                // 飞越 (4)
                45, 32, 4.5D, 5.0D,
                // 悬停 (4)
                24, 4, 42, 45,
                // 鸟浴台 (3)
                1.5D, 1.8D, 32,
                // 起飞与逃离 (6)
                24, 45, 0.35D, 120.0D, 120.0D, 140.0D,
                // 飞行结束 (1)
                0.24D,
                // 降落 (3)
                160, 160, 180,
                // 不安全降落 (3)
                28, 55, 45,
                // 空中巡航目标 (3)
                50, 70.0D, 50.0D,
                // 降落目标搜索 (5)
                60, 36, 46, 3, 2,
                // 飞行高度限制 (2)
                2.0D, 0.8D,
                // 水上逃生 (7)
                90, 50, 70, 0.22, 0.28, 1, 12,
                // 飞行系统参数 (21)
                1.85, 0.35, 0.32, 0.68, 0.006, 0.18, 0.08, 0.34, 0.2, 0.26,
                3.4, 0.42, 0.025, 0.28, -0.035, -0.13, 0.055, -0.075, 0.16, 0.11, 0.12,
                // 群体飞行 (7)
                13.0, 2.4, 0.035, 0.45, 0.18, 0.08, 0.1,
                // 巡航几率 (2)
                170, 150
        );
    }
}