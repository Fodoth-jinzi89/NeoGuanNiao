package net.fodoth.skina.neoguanniao.content.bird.core.data;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;


public record BirdData(

        // ============ 基础属性 ============
        int spawnRarity,
        float voicePitch,
        int ambientSoundInterval,
        BirdGuidePreviewAnimation guidePreviewAnimation,

        // ============ 行走 ============
        double walkingSpeedThreshold,

        // ============ 飞行基础 ============
        BirdFlightProfile flightProfile,
        int minimumFlightTicks,
        int airborneGraceTicks,

        // ============ 环境巡航 ============
        int ambientAirCruiseMinTicks,
        int ambientAirCruiseRandomTicks,
        int escapeAirCruiseMinTicks,
        int escapeAirCruiseRandomTicks,

        // ============ 飞越 ============
        int minFlybyDuration,
        int flybyDurationVariance,
        double flybyHorizontalSpeed,
        double flybyUpwardSpeed,

        // ============ 悬停 ============
        int minHoverRetargetTicks,
        int hoverRetargetTicksVariance,
        int hoverRetargetMinDelay,
        int hoverRetargetDelayVariance,

        // ============ 鸟浴台 ============
        double birdBathMountHorizontalSpeed,
        double birdBathMountUpwardSpeed,
        int birdBathMountFlightTicks,

        int eatingTicksLimitForBath,
        int curiousTicksLimitForBath,

        // ============ 起飞与逃离 ============
        int shortFlyTicks,
        int shortFleeTicks,

        double escapeFlightMinDistance,
        double escapeFlightDistanceVariance,
        double escapeFlightMinHeight,
        double escapeFlightHeightVariance,

        // ============ 飞行结束 ============
        double flightLandingHorizontalDamping,

        int escapeCooldownMin,
        int escapeCooldownVariance,
        int tameCooldownMin,
        int tameCooldownVariance,
        int wildCooldownMin,
        int wildCooldownVariance,

        int postFlightAlertTicks,

        // ============ 降落 ============
        int landingFlightMinDuration,
        int landingFlightDurationVariance,
        int landingFlightStateTicks,

        // ============ 不安全降落 ============
        int unsafeLandingCruiseMinDuration,
        int unsafeLandingCruiseDurationVariance,
        int unsafeLandingCruiseStateTicks,

        // ============ 空中巡航目标 ============
        int cruiseLookChanceDenominator,
        double cruiseFallbackHeightGround,
        double cruiseFallbackHeightAir,

        // ============ 降落目标搜索 ============
        int landingSharedRadius,
        int landingSharedVerticalRange,
        int landingSurfaceSearchRadius,
        int landingRandomAttempts,
        int landingRandomHorizontalRange,

        // ============ 飞行高度限制 ============
        double flightTargetMinHeightOffset,
        double flightTargetMaxHeightOffset,


        // ============ 信任 ============
        int trustTicksLimit,
        double trustShareRange,
        int addTrustValue,
        int addTrustNearbyValue,
        float droppedItemTrustMultiplier,
        int trustTameThreshold,


        // ============ 进食 ============
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


        // ============ 好奇 ============
        int curiousTicksLimitForDroppedFood,
        int curiousTicksLimitForSharedTrust,
        int curiousTicksLimitForAlert,
        int curiousTicksLimitForTame,


        // ============ 惊吓 ============
        int frightTicksLimit,
        float frightenAmount,

        int pendingFrightDurationLimit,
        int pendingFrightTicksLimit,

        int frightDelayMin,
        int frightDelayVariance,


        // ============ 警觉 ============
        double alertNearbyRange,

        int alertTicks,
        int alertTicksVariant,

        int alertTicksPlayer,
        int alertTicksOther,


        // ============ 驯服庆祝 ============
        int tameCelebrationCuriousTicks,
        int tameCelebrationBehaviorStateTicks,

        int tameCelebrationPostTameActionTicksMin,
        int tameCelebrationPostTameActionTicksVariance,
        int tameCelebrationPostTameActionSwapTicks,

        int tameCelebrationFoodTicks,


        // ============ 受伤反应 ============
        float frightenedTrustLossPlayer,
        float frightenedTrustLossOther,

        SoundEvent ambientSound,
        SoundEvent hurtSound,
        SoundEvent deathSound,
        SoundEvent interactionSound,

        ResourceLocation location,
        BirdModelScaleProfile modelScaleProfile,
        int skinVariants,

        int tamedBehaviorTicks,
        int tamedBehaviorTicksVariance,
        int postTameActionSwapTicks,
        int postTameActionSwapTicksVariance,

        int waterEscapeMinDuration,
        int waterEscapeRandomDuration,
        int waterEscapeBehaviorTicks,
        double waterEscapeHorizontalSpeed,
        double waterEscapeVerticalSpeed,
        int waterEscapeHoverRetargetMin,
        int waterEscapeHoverRetargetMax,

        double pendingFrightLookYOffset,
        float pendingFrightLookSpeed,
        int pendingFrightMinDuration,
        // ============ 飞行系统 ============
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
        double flightLandingDecelDistance,
        double flightLandingDecelFactor,
        double flightHoverBobAmplitude,
        double flightHoverBobFrequency,
        double flightLandingHoverBob,
        double flightVerticalClampMin,
        double flightVerticalClampMax,
        double flightVerticalAmbientMin,
        double flightVerticalAmbientMax,
        double flightVerticalLandingFactor,
        double flightVerticalAmbientFactor,

// ============ 群体飞行 ============
        double flockRange,
        double flockSeparation,
        double flockAlignment,
        double flockCohesion,
        double flockEscapeWeight,
        double flockAmbientWeight,
        double flockWeightEscape,

        // ============ 飞行系统 ============
        int ambientAirCruiseChanceTame,
        int ambientAirCruiseChanceWild,

        double followingDistanceThreshold,

        long activeStartTime,
        long activeEndTime,

        int maxTurns,

        float mutantChance



) {


    /**
     * 创建一个包含默认值的 BirdData 实例
     */
    public static BirdData createDefault() {
        return new BirdData(


                8,
                0.5F,
                180,
                BirdGuidePreviewAnimation.NONE,

                0.0025D,

                null,
                90,
                0,

                110,
                120,
                80,
                70,

                45,
                32,
                4.5D,
                5.0D,

                24,
                4,
                42,
                45,

                1.5D,
                1.8D,
                32,

                120,
                120,

                24,
                45,

                0.35D,
                120,
                120,
                140,

                0.24D,

                160,
                160,
                180,
                28,
                55,
                45,

                50,

                70,
                50,
                60,

                36,
                46,
                3,

                2,
                2.0D,
                0.8D,

                8,
                16,
                16,
                24,
                13,

                1.5D,
                2.0D,


                6000,
                10.0D,
                420,
                120,
                0.5F,
                900,


                35,
                21,
                90,
                60,

                0.35F,
                0.45F,
                0.05F,
                1.35F,
                0.2F,
                0.8F,


                180,
                80,
                40,
                260,


                90,
                0.06F,

                24,
                24,

                18,
                16,


                14.0D,

                24,
                45,

                35,
                55,


                220,
                55,

                150,
                50,
                42,

                45,


                0.08F,
                0.18F,
                null,
                null,
                null,
                null,
                null,
                null,
                1,

                32,
                32,
                30,
                28,

                90,
                50,
                70,
                0.22,
                0.28,
                1,
                12,

                0.6F,
                35.0F,
                60,

                1.85,
                0.35,
                0.32,
                0.68,
                0.006,
                0.18,
                0.08,
                0.34,
                0.2,
                0.26,
                3.4,
                0.42,
                0.025,
                0.28,
                -0.035,
                -0.13,
                0.055,
                -0.075,
                0.16,
                0.11,
                0.12,
                13.0,
                2.4,
                0.035,
                0.45,
                0.18,
                0.08,
                0.1,

                170,
                150,

                9.0D,

                23000L,
                11500L,
                10,
                0.05F


        );
    }

}