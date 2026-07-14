package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;

/**
 * 鸟类驯服后行为持续计时器
 * <p>
 * 负责管理驯服成功后的庆祝行为周期。
 * 控制驯服成功后的一段特殊行为周期，
 * 包括好奇、整理羽毛以及观察主人等动作。
 * 该计时器在服务端和客户端均执行。
 * </p>
 */
public class BirdPostTameActionTicker extends AbstractBirdTicker {

    /**
     * 创建驯服后行为持续计时器（在服务端和客户端均执行）
     *
     * @param bird 鸟类实体
     */
    public BirdPostTameActionTicker(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    /**
     * 更新驯服后的庆祝行为
     * <p>
     * 控制驯服成功后的一段特殊行为周期，
     * 包括好奇、整理羽毛以及观察主人等动作。
     * </p>
     */
    @Override
    protected void run() {
        // 计时器归零时不处理
        if (ticks <= 0) {
            return;
        }

        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var stateController = bird.getBehaviorStateController();
        var eatingController = bird.getEatingController();
        BirdData birdData = bird.getBirdData();
        var random = bird.getRandom();

        // 减少计时器
        --ticks;

        // 如果鸟被骑乘，不执行任何行为
        if (bird.isPassenger()) {
            return;
        }

        // 清除进食状态
        if (eatingController.isEating()) {
            eatingController.clearEating();
        }

        // 获取当前行为状态
        BirdBehaviorState currentState = stateController.getBehaviorState();

        // 如果正在睡眠或栖息，转为好奇状态
        if (currentState == BirdBehaviorState.SLEEPING || currentState == BirdBehaviorState.ROOSTING) {
            timer.getBirdBehaviorStateTicker().setTicks(0);
            stateController.setBehaviorState(BirdBehaviorState.CURIOUS);
        }

        // 定期注视主人（每 8 刻更新一次）
        if (bird.getOwner() != null && bird.tickCount % 8 == 0) {
            bird.getLookControl().setLookAt(bird.getOwner(), 35.0F, 35.0F);
        }

        // 获取相关计时器
        var postTameSwapTicker = timer.getBirdPostTameActionSwapTicker();
        var behaviorStateTicker = timer.getBirdBehaviorStateTicker();

        // 切换驯服后行为（好奇/整理羽毛）
        boolean shouldSwitch = postTameSwapTicker.getTicks() <= 0 || currentState == BirdBehaviorState.IDLE;
        if (shouldSwitch) {
            // 随机选择好奇或整理羽毛
            BirdBehaviorState newState = random.nextBoolean()
                    ? BirdBehaviorState.CURIOUS
                    : BirdBehaviorState.PREENING;

            // 计算行为持续时间
            int baseTicks = birdData.tame().tamedBehaviorTicks();
            int variance = birdData.tame().tamedBehaviorTicksVariance();
            int behaviorTicks = baseTicks + random.nextInt(variance);

            // 设置行为状态
            stateController.setBehaviorStateFor(newState, behaviorTicks);

            // 重置行为切换计时器
            int swapBase = birdData.tame().postTameActionSwapTicks();
            int swapVariance = birdData.tame().postTameActionSwapTicksVariance();
            postTameSwapTicker.setTicks(swapBase + random.nextInt(swapVariance));
        }

        // 如果切换计时器结束且处于好奇或整理羽毛状态，回到空闲状态
        if (postTameSwapTicker.getTicks() <= 0) {
            boolean isCuriousOrPreening = currentState == BirdBehaviorState.CURIOUS
                    || currentState == BirdBehaviorState.PREENING;
            if (isCuriousOrPreening) {
                behaviorStateTicker.setTicks(0);
                stateController.setBehaviorState(BirdBehaviorState.IDLE);
            }
        }
    }
}