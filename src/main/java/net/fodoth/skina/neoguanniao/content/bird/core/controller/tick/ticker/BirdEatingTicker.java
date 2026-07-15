package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

/**
 * 鸟类进食计时器
 * <p>
 * 负责管理鸟类进食行为的持续时间。
 * 进食期间会停止移动并保持 EATING 行为状态，
 * 时间结束后自动清除进食状态。
 * 该计时器在服务端和客户端均执行。
 * </p>
 */
public class BirdEatingTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    public BirdEatingTicker() {
        super();
    }

    /**
     * 更新进食状态
     * <p>
     * 进食期间停止移动并保持 EATING 行为状态，
     * 时间结束后清除进食状态。
     * </p>
     */
    @Override
    protected void run() {
        var stateController = bird().getBehaviorStateController();
        // 停止移动并保持进食状态
        bird().getNavigation().stop();
        stateController.setBehaviorState(BirdBehaviorState.EATING);
    }

    @Override
    protected void onExpire() {
        bird().getEatingController().clearEating();
    }
}