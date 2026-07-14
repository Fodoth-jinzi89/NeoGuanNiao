package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.BEHAVIOR_STATE;

/**
 * 鸟类行为状态控制器。
 * <p>
 * 负责同步、读取和修改鸟类当前的行为状态，并提供行为状态锁定功能。
 */
public class BirdBehaviorStateController {
    public BirdBehaviorState behaviorState;
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类行为状态控制器。
     *
     * @param entity 鸟类实体
     */
    public BirdBehaviorStateController(AbstractBirdEntity<?> entity) {
        this.bird = entity;
        this.behaviorState = BirdBehaviorState.IDLE;
    }

    /**
     * 从实体同步当前行为状态。
     */
    public void decodeBehaviorState() {
        this.behaviorState = getBehaviorState();
    }

    /**
     * 获取当前行为状态。
     *
     * @return 当前行为状态
     */
    public BirdBehaviorState getBehaviorState() {
        int ordinal = bird.getEntityData().get(BEHAVIOR_STATE);
        BirdBehaviorState[] values = BirdBehaviorState.values();
        return (ordinal >= 0 && ordinal < values.length ? values[ordinal] : BirdBehaviorState.IDLE);
    }


    /**
     * 设置当前行为状态。
     * <p>
     * 如果传入状态为 {@code null}，则使用 {@link BirdBehaviorState#IDLE}。
     *
     * @param state 行为状态
     */
    public void setBehaviorState(BirdBehaviorState state) {
        if (state == null) {
            state = BirdBehaviorState.IDLE;
        }
        this.behaviorState = state;
        bird.getEntityData().set(BEHAVIOR_STATE, state.ordinal());
    }

    /**
     * 设置当前行为状态，并锁定指定时间。
     * <p>
     * 在锁定期间，行为状态不会被其它逻辑覆盖。
     *
     * @param state 行为状态
     * @param ticks 锁定持续 Tick 数
     */
    public void setBehaviorStateFor(BirdBehaviorState state, int ticks) {
        this.setBehaviorState(state);
        bird.getTickController().behaviorStateLockTicks = Math.max(bird.getTickController().behaviorStateLockTicks, ticks);
    }

}
