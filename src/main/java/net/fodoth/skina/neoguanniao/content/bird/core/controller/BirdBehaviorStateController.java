package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.BEHAVIOR_STATE;

/**
 * 鸟类行为状态控制器
 * <p>
 * 负责同步、读取和修改鸟类当前的行为状态，并提供行为状态锁定功能。
 * 行为状态控制鸟类的整体行为模式，如空闲、飞行、着陆等。
 * </p>
 *
 */
public class BirdBehaviorStateController {

    /**
     * 当前行为状态，默认为空闲状态
     */
    public BirdBehaviorState behaviorState;

    /**
     * 关联的鸟类实体
     */
    private final AbstractBirdEntity<?> bird;

    /**
     * 构造鸟类行为状态控制器
     *
     * @param entity 鸟类实体，不能为 null
     */
    public BirdBehaviorStateController(AbstractBirdEntity<?> entity) {
        this.bird = entity;
        this.behaviorState = BirdBehaviorState.IDLE;
    }

    /**
     * 从实体数据中同步当前行为状态
     * <p>
     * 从实体的 {@link net.minecraft.world.entity.Entity#getEntityData()} 中读取
     * 行为状态并更新到控制器中。
     * </p>
     */
    public void decodeBehaviorState() {
        this.behaviorState = getBehaviorState();
    }

    /**
     * 获取当前行为状态
     * <p>
     * 从实体的数据存储中读取行为状态的序号，并将其转换为对应的枚举值。
     * 如果序号无效，则返回默认的空闲状态。
     * </p>
     *
     * @return 当前行为状态，不为 null
     */
    public BirdBehaviorState getBehaviorState() {
        // 从实体数据中读取行为状态的序号
        int ordinal = bird.getEntityData().get(BEHAVIOR_STATE);

        // 获取所有行为状态枚举值
        BirdBehaviorState[] values = BirdBehaviorState.values();

        // 验证序号有效性，无效则返回默认空闲状态
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BirdBehaviorState.IDLE;
    }

    /**
     * 设置当前行为状态
     * <p>
     * 如果传入状态为 {@code null}，则使用 {@link BirdBehaviorState#IDLE} 作为默认值。
     * 设置后会将状态同步到实体的数据存储中。
     * </p>
     *
     * @param state 要设置的行为状态，可以为 null
     */
    public void setBehaviorState(BirdBehaviorState state) {
        // 确保状态不为 null
        BirdBehaviorState targetState = state != null ? state : BirdBehaviorState.IDLE;

        // 更新控制器中的状态
        this.behaviorState = targetState;

        // 同步到实体数据存储
        bird.getEntityData().set(BEHAVIOR_STATE, targetState.ordinal());
    }

    /**
     * 设置当前行为状态，并锁定指定时间
     * <p>
     * 在锁定期间，行为状态不会被其它逻辑覆盖。
     * 锁定时间会与现有的锁定时间取最大值，以延长锁定效果。
     * </p>
     *
     * @param state 要设置的行为状态，可以为 null
     * @param ticks 锁定持续 Tick 数，必须大于等于 0
     */
    public void setBehaviorStateFor(BirdBehaviorState state, int ticks) {
        // 设置行为状态
        this.setBehaviorState(state);

        // 获取行为状态计时器并设置锁定时间
        // 使用 Math.max 确保锁定时间不会缩短
        var timer = bird.getTickController().getTickTimer();
        var stateTicker = timer.getBirdBehaviorStateTicker();
        int currentTicks = stateTicker.getTicks();
        int newTicks = Math.max(currentTicks, ticks);
        stateTicker.setTicks(newTicks);
    }
}