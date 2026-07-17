package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightAware;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 鸟类动画控制器
 *
 * <p>
 * 负责管理 GeckoLib 动画实例、注册动画控制器，
 * 并根据鸟类当前状态判断应播放的动画类型。
 * </p>
 */
public class BirdAnimationController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {

    /**
     * 当前待播放的待机动画
     */
    private RawAnimation currentIdleAnimation;

    /**
     * 当前引导预览动画
     */
    private RawAnimation currentGuideAnimation;



    /**
     * GeckoLib 动画实例缓存
     */
    private AnimatableInstanceCache cache;


    public BirdAnimationController() {
    }


    /**
     * Controller 绑定实体后的初始化
     */
    @Override
    protected void onAttach() {
        super.onAttach();

        this.cache = GeckoLibUtil.createInstanceCache(bird());

        this.currentIdleAnimation = pickIdleAnimation();
        setGuidePreviewAnimation(null);
    }


    /**
     * 获取 GeckoLib 动画缓存
     */
    public AnimatableInstanceCache cache() {
        if (cache == null) {
            throw new IllegalStateException(
                    "BirdAnimationController is not attached"
            );
        }

        return cache;
    }


    /**
     * 获取当前待机动画
     *
     * @return 当前待机动画
     */
    public RawAnimation pickIdleAnimation() {

        var bird = bird();

        var tickController = bird.getTickController();
        var tickTimer = tickController.getTickTimer();
        var birdData = bird.getBirdData();
        var animationDatum = birdData.animation();

        int idleTicker = tickTimer.getBirdIdleAnimationTicker().getTicks();
        int trustTicker = tickTimer.getBirdTrustTicker().getTicks();
        int curiousTicker = tickTimer.getBirdCuriousTicker().getTicks();


        if (idleTicker <= 0) {

            int randomMax = getIdleAnimationRollMax(
                    trustTicker,
                    curiousTicker
            );

            int roll = bird.getRandom().nextInt(randomMax);


            if (roll == 0) {

                this.currentIdleAnimation =
                        animationDatum.animationMap().get("preen");

                int duration =
                        animationDatum.preenDuration()
                                + bird.getRandom()
                                .nextInt(animationDatum.preenDurationVariance());

                tickTimer.getBirdIdleAnimationTicker()
                        .setTicks(duration);


            } else if (
                    roll > 2
                            || shouldUseIdleAnimation(
                            trustTicker,
                            curiousTicker
                    )
            ) {

                this.currentIdleAnimation =
                        animationDatum.animationMap().get("idle");


                int duration =
                        animationDatum.idleDuration()
                                + bird.getRandom()
                                .nextInt(animationDatum.idleDurationVariance());


                tickTimer.getBirdIdleAnimationTicker()
                        .setTicks(duration);


            } else {

                int duration =
                        animationDatum.otherDuration()
                                + bird.getRandom()
                                .nextInt(animationDatum.otherDurationVariance());


                tickTimer.getBirdIdleAnimationTicker()
                        .setTicks(duration);
            }
        }


        return currentIdleAnimation;
    }


    /**
     * 注册 GeckoLib 动画控制器
     */
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        bird(),
                        "movement",
                        4,
                        bird()::movementController
                )
        );
    }


    /**
     * 判断是否播放飞行动画
     */
    public boolean shouldPlayFlyAnimation() {

        var bird = bird();

        return shouldPlayFlyAnimation(
                bird,
                bird.getBehaviorStateController()
                        .getBehaviorState()
                        .isAirborne(),

                bird.onGround(),

                bird.isInWater(),

                bird.getDeltaMovement(),

                bird.getBirdData()
                        .flying()
                        .airborneGraceTicks()
        );
    }


    private static boolean shouldPlayFlyAnimation(
            BirdFlightAware bird,
            boolean airborneState,
            boolean onGround,
            boolean noGravity,
            Vec3 movement,
            int airborneGraceTicks
    ) {

        if (!bird.isBirdFlightActive() && !airborneState) {

            if (onGround) {
                return false;
            }


            if (airborneGraceTicks > 0) {
                return true;
            }


            if (!noGravity
                    && !bird.isBirdLanding()
                    && !bird.isBirdEscaping()) {


                if (movement.y > -0.85 && Math.abs(movement.y) > 0.001) {
                    return true;
                }


                return movement.lengthSqr() > 0.001;
            }


            return true;
        }


        return true;
    }


    public void setGuidePreviewAnimation(
            RawAnimation guidePreviewAnimation
    ) {

        this.currentGuideAnimation =
                guidePreviewAnimation == null
                        ? BirdGuidePreviewAnimation.NONE.animation()
                        : guidePreviewAnimation;
    }


    private int getIdleAnimationRollMax(
            int trustTicker,
            int curiousTicker
    ) {

        var birdData = bird().getBirdData();
        var animationDatum = birdData.animation();


        boolean isCuriousAndTrusting =
                trustTicker <= animationDatum.trustTickerMaxLimit()
                        && curiousTicker <= 0;


        return isCuriousAndTrusting
                ? animationDatum.maxCuriousAndTrustingIndex()
                : animationDatum.minCuriousAndTrustingIndex();
    }


    private boolean shouldUseIdleAnimation(
            int trustTicker,
            int curiousTicker
    ) {

        var animationDatum =
                bird().getBirdData().animation();


        return trustTicker <= animationDatum.trustTickerLimit()
                && curiousTicker <= 0;
    }


    @Override
    public void tick() {
        super.tick();
    }

    public RawAnimation getCurrentGuideAnimation() {
        return currentGuideAnimation;
    }

    public AnimatableInstanceCache getCache() {
        return cache;
    }
}