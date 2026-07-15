package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类目标控制器
 * <p>
 * 提供各类 AI Goal 的启动条件判断。
 * </p>
 */
public class BirdGoalController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T>{

    /**
     * 判断是否可以开始觅食目标
     *
     * @return 如果可以开始觅食返回 true
     */
    public boolean canStartFoodGoal() {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var eatingController = bird.getEatingController();
        var routineController = bird.getRoutineController();
        var stateController = bird.getBehaviorStateController();

        boolean hasNoFoodTicks = timer.getBirdFoodTicker().getTicks() <= 0;
        boolean isNotEating = !eatingController.isEating();
        boolean isNotPassenger = !bird.isPassenger();
        boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
        boolean isNotEscaping = !stateController.getBehaviorState().isEscape();

        return hasNoFoodTicks && isNotEating && isNotPassenger
                && isNotSleepingOrRoosting && isNotEscaping;
    }

    /**
     * 判断是否可以开始社交目标
     *
     * @return 如果可以开始社交返回 true
     */
    public boolean canStartSocialGoal() {
        var routineController = bird.getRoutineController();
        var eatingController = bird.getEatingController();
        var stateController = bird.getBehaviorStateController();

        boolean isActiveTime = routineController.isActiveTime();
        boolean isNotEating = !eatingController.isEating();
        boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
        boolean isNotEscaping = !stateController.getBehaviorState().isEscape();

        return isActiveTime && isNotEating && isNotSleepingOrRoosting && isNotEscaping;
    }
}