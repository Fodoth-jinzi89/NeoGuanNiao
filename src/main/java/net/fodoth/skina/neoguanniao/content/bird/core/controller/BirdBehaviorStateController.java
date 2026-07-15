package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.BEHAVIOR_STATE;

/**
 * 鸟类行为状态控制器
 *
 * <p>
 * 负责同步、读取和修改鸟类当前的行为状态，
 * 并提供行为状态锁定功能。
 * </p>
 */
public class BirdBehaviorStateController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {

    /**
     * 当前行为状态，默认为空闲状态
     */
    private BirdBehaviorState behaviorState;


    public BirdBehaviorStateController() {
        this.behaviorState = BirdBehaviorState.IDLE;
    }


    /**
     * Controller 绑定实体后的初始化
     */
    @Override
    protected void onAttach() {
        super.onAttach();

        decodeBehaviorState();
    }


    /**
     * 从实体数据中同步当前行为状态
     */
    public void decodeBehaviorState() {
        this.behaviorState = getBehaviorState();
    }


    /**
     * 获取当前行为状态
     *
     * @return 当前行为状态
     */
    public BirdBehaviorState getBehaviorState() {

        int ordinal = bird()
                .getEntityData()
                .get(BEHAVIOR_STATE);


        BirdBehaviorState[] values = BirdBehaviorState.values();


        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }


        return BirdBehaviorState.IDLE;
    }


    /**
     * 设置当前行为状态
     *
     * @param state 行为状态
     */
    public void setBehaviorState(BirdBehaviorState state) {

        BirdBehaviorState targetState =
                state != null
                        ? state
                        : BirdBehaviorState.IDLE;


        this.behaviorState = targetState;


        bird()
                .getEntityData()
                .set(
                        BEHAVIOR_STATE,
                        targetState.ordinal()
                );
    }


    /**
     * 设置行为状态并锁定指定时间
     *
     * @param state 行为状态
     * @param ticks 锁定时间
     */
    public void setBehaviorStateFor(
            BirdBehaviorState state,
            int ticks
    ) {

        setBehaviorState(state);


        var timer =
                bird()
                        .getTickController()
                        .getTickTimer();


        var stateTicker =
                timer.getBirdBehaviorStateTicker();


        int currentTicks =
                stateTicker.getTicks();


        stateTicker.setTicks(
                Math.max(currentTicks, ticks)
        );
    }


    /**
     * 获取控制器缓存中的行为状态
     *
     * <p>
     * 与 {@link #getBehaviorState()} 不同，
     * 该方法不会读取 EntityData。
     * </p>
     */
    public BirdBehaviorState behaviorState() {
        return behaviorState;
    }
}