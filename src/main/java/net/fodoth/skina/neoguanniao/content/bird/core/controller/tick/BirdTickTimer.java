package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker.*;

import java.util.List;

/**
 * 鸟类 Tick 计时器管理器
 *
 * <p>
 * 负责管理和驱动所有鸟类基于 Tick 的计时器。
 * </p>
 */
public class BirdTickTimer<T extends AbstractBirdEntity<T>> extends AbstractBirdTimer<T> {


    private final BirdBehaviorStateTicker<T> birdBehaviorStateTicker;
    private final BirdCuriousTicker<T> birdCuriousTicker;
    private final BirdEatingTicker<T> birdEatingTicker;
    private final BirdFlyingTicker<T> birdFlyingTicker;
    private final BirdFoodTicker<T> birdFoodTicker;
    private final BirdPendingFrightTicker<T> birdPendingFrightTicker;
    private final BirdExternalFrightTicker<T> birdExternalFrightTicker;
    private final BirdIdleAnimationTicker<T> birdIdleAnimationTicker;
    private final BirdPostTameActionSwapTicker<T> birdPostTameActionSwapTicker;
    private final BirdPostTameActionTicker<T> birdPostTameActionTicker;
    private final BirdTrustTicker<T> birdTrustTicker;
    private final BirdFindNearbyMusicLoopTicker<T> birdFindNearbyMusicLoopTicker;
    private final BirdMusicTicker<T> birdMusicTicker;


    private final List<AbstractBirdTicker<T>> tickers;


    public BirdTickTimer() {

        birdBehaviorStateTicker =
                new BirdBehaviorStateTicker<>();

        birdCuriousTicker =
                new BirdCuriousTicker<>();

        birdEatingTicker =
                new BirdEatingTicker<>();

        birdFlyingTicker =
                new BirdFlyingTicker<>();

        birdFoodTicker =
                new BirdFoodTicker<>();

        birdPendingFrightTicker =
                new BirdPendingFrightTicker<>();

        birdExternalFrightTicker =
                new BirdExternalFrightTicker<>();

        birdIdleAnimationTicker =
                new BirdIdleAnimationTicker<>();

        birdPostTameActionSwapTicker =
                new BirdPostTameActionSwapTicker<>();

        birdPostTameActionTicker =
                new BirdPostTameActionTicker<>();

        birdTrustTicker =
                new BirdTrustTicker<>();

        birdFindNearbyMusicLoopTicker =
                new BirdFindNearbyMusicLoopTicker<>();

        birdMusicTicker =
                new BirdMusicTicker<>();


        tickers = List.of(
                birdBehaviorStateTicker,
                birdCuriousTicker,
                birdEatingTicker,
                birdFlyingTicker,
                birdFoodTicker,
                birdPendingFrightTicker,
                birdExternalFrightTicker,
                birdIdleAnimationTicker,
                birdPostTameActionSwapTicker,
                birdPostTameActionTicker,
                birdTrustTicker,
                birdFindNearbyMusicLoopTicker,
                birdMusicTicker
        );
    }


    /**
     * 绑定鸟实体
     */
    @Override
    protected void onAttach() {

        for (AbstractBirdTicker<T> ticker : tickers) {
            ticker.attach(bird());
        }
    }


    /**
     * 更新服务端 Tick
     */
    @Override
    public void tick() {

        for (AbstractBirdTicker<T> ticker : tickers) {
            ticker.tick();
        }
    }


    /**
     * 更新客户端 Tick
     */
    @Override
    public void tickClient() {

        for (AbstractBirdTicker<T> ticker : tickers) {
            ticker.tickClient();
        }
    }


    public List<AbstractBirdTicker<T>> getTickers() {
        return tickers;
    }


    public BirdBehaviorStateTicker<T> getBirdBehaviorStateTicker() {
        return birdBehaviorStateTicker;
    }


    public BirdCuriousTicker<T> getBirdCuriousTicker() {
        return birdCuriousTicker;
    }


    public BirdEatingTicker<T> getBirdEatingTicker() {
        return birdEatingTicker;
    }


    public BirdFlyingTicker<T> getBirdFlyingTicker() {
        return birdFlyingTicker;
    }


    public BirdFoodTicker<T> getBirdFoodTicker() {
        return birdFoodTicker;
    }


    public BirdPendingFrightTicker<T> getBirdPendingFrightTicker() {
        return birdPendingFrightTicker;
    }


    public BirdExternalFrightTicker<T> getBirdExternalFrightTicker() {
        return birdExternalFrightTicker;
    }


    public BirdIdleAnimationTicker<T> getBirdIdleAnimationTicker() {
        return birdIdleAnimationTicker;
    }


    public BirdPostTameActionSwapTicker<T> getBirdPostTameActionSwapTicker() {
        return birdPostTameActionSwapTicker;
    }


    public BirdPostTameActionTicker<T> getBirdPostTameActionTicker() {
        return birdPostTameActionTicker;
    }


    public BirdTrustTicker<T> getBirdTrustTicker() {
        return birdTrustTicker;
    }


    public BirdFindNearbyMusicLoopTicker<T> getBirdFindNearbyMusicLoopTicker() {
        return birdFindNearbyMusicLoopTicker;
    }

    public BirdMusicTicker<T> getBirdMusicTicker() {
        return birdMusicTicker;
    }
}