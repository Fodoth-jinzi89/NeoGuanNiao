package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类目标控制器。
 * <p>
 * 提供各类 AI Goal 的启动条件判断。
 */
public record BirdGoalController(AbstractBirdEntity<?> bird) {

    /**
     * 判断是否可以开始觅食目标。
     *
     * @return 如果可以开始觅食返回 true
     */
    public boolean canStartFoodGoal() {
        return bird.getTickController().foodTicks <= 0 && !bird.getEatingController().isEating() && !bird.isPassenger()
                && !bird.getRoutineController().isSleepingOrRoosting()
                && !bird.getBehaviorStateController().getBehaviorState().isEscape();
    }

    /**
     * 判断是否可以开始社交目标。
     *
     * @return 如果可以开始社交返回 true
     */
    public boolean canStartSocialGoal() {
        return bird.getRoutineController().isActiveTime() && !bird.getEatingController().isEating() && !bird.getRoutineController().isSleepingOrRoosting() && !bird.getBehaviorStateController().getBehaviorState().isEscape();
    }
}
