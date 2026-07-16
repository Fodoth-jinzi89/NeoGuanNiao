package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

/**
 * 鸟类行为状态计时器
 * <p>
 * 负责更新并修正鸟类的默认行为状态。
 * 当没有特殊行为（进食、飞行、受惊等）占用时，
 * 根据环境条件、驯服状态和移动情况选择合适的状态。
 * </p>
 */
public class BirdBehaviorStateTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{


    public BirdBehaviorStateTicker() {
        super();
    }

    /**
     * 修正默认行为状态
     * <p>
     * 当没有特殊行为占用时，根据环境、驯服状态和移动情况选择：
     * <ul>
     *     <li>睡眠</li>
     *     <li>跟随主人</li>
     *     <li>行走</li>
     *     <li>空闲</li>
     * </ul>
     * </p>
     */
    @Override
    protected void onExpire() {
        super.onExpire();
        var tickController = bird().getTickController();
        var timer = tickController.getTickTimer();
        var stateController = bird().getBehaviorStateController();
        var eatingController = bird().getEatingController();
        var routineController = bird().getRoutineController();
        var birdData = bird().getbirdData();
        var currentState = stateController.getBehaviorState();
        var postTameActionTicker = timer.getBirdPostTameActionTicker();

        // 只有在无特殊行为占用时才修正状态
        boolean hasNoSpecialState = postTameActionTicker.getTicks() <= 0
                && !eatingController.isEating()
                && !bird().isPassenger();

        if (!hasNoSpecialState) {
            return;
        }

        // 检查是否应该进入睡眠状态
        boolean shouldSleep = routineController.isRoostTime()
                && bird().getNavigation().isDone();

        if (shouldSleep) {
            stateController.setBehaviorState(BirdBehaviorState.SLEEPING);
            return;
        }

        // 检查是否处于逃跑或飞行状态
        if (currentState == BirdBehaviorState.FLEEING || currentState == BirdBehaviorState.FLYING) {
            stateController.setBehaviorState(BirdBehaviorState.ALERT);
            return;
        }

        // 检查是否应该跟随主人
        boolean isTame = bird().isTame();
        boolean hasOwner = bird().getOwner() != null;
        boolean isNavigating = !bird().getNavigation().isDone();
        double distanceToOwnerSqr = hasOwner ? bird().distanceToSqr(bird().getOwner()) : 0;
        double followingThreshold = birdData.misc().followingDistanceThreshold();

        if (isTame && hasOwner && isNavigating && distanceToOwnerSqr > followingThreshold) {
            stateController.setBehaviorState(BirdBehaviorState.FOLLOWING);
            return;
        }

        // 根据移动情况选择行走或空闲状态
        double movementSpeedSqr = bird().getDeltaMovement().lengthSqr();
        double walkingThreshold = birdData.misc().walkingSpeedThreshold();
        boolean isMoving = movementSpeedSqr > walkingThreshold;
        boolean isDoneNavigating = bird().getNavigation().isDone();

        if (!isMoving && isDoneNavigating) {
            // 如果当前是临时状态，则切换到空闲
            if (currentState == BirdBehaviorState.WALKING
                    || currentState == BirdBehaviorState.FORAGING
                    || currentState == BirdBehaviorState.FOLLOWING
                    || currentState == BirdBehaviorState.ALERT) {
                stateController.setBehaviorState(BirdBehaviorState.IDLE);
            }
        } else {
            stateController.setBehaviorState(BirdBehaviorState.WALKING);
        }
    }
}