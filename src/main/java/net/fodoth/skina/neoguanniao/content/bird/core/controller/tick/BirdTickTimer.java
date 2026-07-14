package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 鸟类 Tick 计时器管理器
 * <p>
 * 负责管理和驱动所有鸟类基于 Tick 的计时器，
 * 包括行为状态、好奇、进食、飞行、食物效果、受惊、空闲动画、
 * 驯服后行为切换、驯服后行为持续、信任值等计时器。
 * </p>
 */
public class BirdTickTimer {

    /**
     * 行为状态计时器
     */
    private final BirdBehaviorStateTicker birdBehaviorStateTicker;

    /**
     * 好奇状态计时器
     */
    private final BirdCuriousTicker birdCuriousTicker;

    /**
     * 进食计时器
     */
    private final BirdEatingTicker birdEatingTicker;

    /**
     * 飞行计时器
     */
    private final BirdFlyingTicker birdFlyingTicker;

    /**
     * 食物效果计时器
     */
    private final BirdFoodTicker birdFoodTicker;

    /**
     * 待处理受惊计时器
     */
    private final BirdPendingFrightTicker birdPendingFrightTicker;

    /**
     * 外部受惊计时器
     */
    private final BirdExternalFrightTicker birdExternalFrightTicker;

    /**
     * 空闲动画计时器
     */
    private final BirdIdleAnimationTicker birdIdleAnimationTicker;

    /**
     * 驯服后行为切换计时器
     */
    private final BirdPostTameActionSwapTicker birdPostTameActionSwapTicker;

    /**
     * 驯服后行为持续计时器
     */
    private final BirdPostTameActionTicker birdPostTameActionTicker;

    /**
     * 信任值计时器
     */
    private final BirdTrustTicker birdTrustTicker;

    /**
     * 所有计时器列表，用于统一更新
     */
    private final List<AbstractBirdTicker> tickers = new ArrayList<>();

    /**
     * 创建鸟类 Tick 计时器管理器
     *
     * @param bird 鸟类实体
     */
    public BirdTickTimer(AbstractBirdEntity<?> bird) {
        // 初始化所有计时器
        birdBehaviorStateTicker = new BirdBehaviorStateTicker(bird);
        birdCuriousTicker = new BirdCuriousTicker(bird);
        birdEatingTicker = new BirdEatingTicker(bird);
        birdFlyingTicker = new BirdFlyingTicker(bird);
        birdFoodTicker = new BirdFoodTicker(bird);
        birdPendingFrightTicker = new BirdPendingFrightTicker(bird);
        birdExternalFrightTicker = new BirdExternalFrightTicker(bird);
        birdIdleAnimationTicker = new BirdIdleAnimationTicker(bird);
        birdPostTameActionSwapTicker = new BirdPostTameActionSwapTicker(bird);
        birdPostTameActionTicker = new BirdPostTameActionTicker(bird);
        birdTrustTicker = new BirdTrustTicker(bird);

        // 将所有计时器添加到统一列表中
        tickers.add(birdBehaviorStateTicker);
        tickers.add(birdCuriousTicker);
        tickers.add(birdEatingTicker);
        tickers.add(birdFlyingTicker);
        tickers.add(birdFoodTicker);
        tickers.add(birdPendingFrightTicker);
        tickers.add(birdExternalFrightTicker);
        tickers.add(birdIdleAnimationTicker);
        tickers.add(birdPostTameActionSwapTicker);
        tickers.add(birdPostTameActionTicker);
        tickers.add(birdTrustTicker);
    }

    /**
     * 更新服务端状态计时器
     * <p>
     * 减少各种持续时间计数，并处理信任值自然衰减。
     * </p>
     */
    public void runTick() {
        for (AbstractBirdTicker ticker : tickers) {
            ticker.tick();
        }
    }

    /**
     * 更新客户端表现计时器
     * <p>
     * 处理客户端动画相关状态。
     * </p>
     */
    public void runClientTick() {
        for (AbstractBirdTicker ticker : tickers) {
            ticker.tickClient();
        }
    }

    /**
     * 获取所有计时器列表
     *
     * @return 所有计时器列表
     */
    public List<AbstractBirdTicker> getTickers() {
        return tickers;
    }

    /**
     * 获取行为状态计时器
     *
     * @return 行为状态计时器
     */
    public BirdBehaviorStateTicker getBirdBehaviorStateTicker() {
        return birdBehaviorStateTicker;
    }

    /**
     * 获取好奇状态计时器
     *
     * @return 好奇状态计时器
     */
    public BirdCuriousTicker getBirdCuriousTicker() {
        return birdCuriousTicker;
    }

    /**
     * 获取进食计时器
     *
     * @return 进食计时器
     */
    public BirdEatingTicker getBirdEatingTicker() {
        return birdEatingTicker;
    }

    /**
     * 获取飞行计时器
     *
     * @return 飞行计时器
     */
    public BirdFlyingTicker getBirdFlyingTicker() {
        return birdFlyingTicker;
    }

    /**
     * 获取食物效果计时器
     *
     * @return 食物效果计时器
     */
    public BirdFoodTicker getBirdFoodTicker() {
        return birdFoodTicker;
    }

    /**
     * 获取待处理受惊计时器
     *
     * @return 待处理受惊计时器
     */
    public BirdPendingFrightTicker getBirdPendingFrightTicker() {
        return birdPendingFrightTicker;
    }

    /**
     * 获取外部受惊计时器
     *
     * @return 外部受惊计时器
     */
    public BirdExternalFrightTicker getBirdExternalFrightTicker() {
        return birdExternalFrightTicker;
    }

    /**
     * 获取空闲动画计时器
     *
     * @return 空闲动画计时器
     */
    public BirdIdleAnimationTicker getBirdIdleAnimationTicker() {
        return birdIdleAnimationTicker;
    }

    /**
     * 获取驯服后行为切换计时器
     *
     * @return 驯服后行为切换计时器
     */
    public BirdPostTameActionSwapTicker getBirdPostTameActionSwapTicker() {
        return birdPostTameActionSwapTicker;
    }

    /**
     * 获取驯服后行为持续计时器
     *
     * @return 驯服后行为持续计时器
     */
    public BirdPostTameActionTicker getBirdPostTameActionTicker() {
        return birdPostTameActionTicker;
    }

    /**
     * 获取信任值计时器
     *
     * @return 信任值计时器
     */
    public BirdTrustTicker getBirdTrustTicker() {
        return birdTrustTicker;
    }
}