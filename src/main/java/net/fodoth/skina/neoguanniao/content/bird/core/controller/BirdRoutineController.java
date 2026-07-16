package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;

/**
 * 鸟类日常行为控制器
 * <p>
 * 负责管理鸟实体基于时间周期的日常活动状态判断，
 * 包括：
 * <ul>
 *     <li>活动时间（Active Time）判断</li>
 *     <li>栖息时间（Roost Time）判断</li>
 *     <li>睡眠与栖息行为状态判断</li>
 * </ul>
 * 该控制器不直接改变鸟的行为状态，
 * 而是为 AI、Tick 系统等提供当前环境状态查询。
 * </p>
 *
 */
public class BirdRoutineController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T>{

    /**
     * Minecraft 世界一天的 Tick 长度
     */
    private static final long DAY_LENGTH = 24000L;

    /**
     * 判断当前是否处于鸟类活动时间
     * <p>
     * 根据鸟种 {@link BirdData} 中定义的活动时间范围进行判断。
     * 活动时间允许跨越午夜，例如夜行鸟的活动时间可以从夜晚持续到凌晨。
     * </p>
     *
     * @return 当前是否为活动时间
     */
    public boolean isActiveTime() {
        BirdData birdData = bird.getbirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        long activeStart = miscDatum.activeStartTime();
        long activeEnd = miscDatum.activeEndTime();
        long time = bird.level().getDayTime() % DAY_LENGTH;

        return time >= activeStart || time < activeEnd;
    }

    /**
     * 判断当前是否处于鸟类栖息时间
     * <p>
     * 栖息时间为活动时间之外的时间段，
     * 通常用于触发停留、休息、寻找栖息点等行为。
     * </p>
     *
     * @return 当前是否为栖息时间
     */
    public boolean isRoostTime() {
        BirdData birdData = bird.getbirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        long activeStart = miscDatum.activeStartTime();
        long activeEnd = miscDatum.activeEndTime();
        long time = bird.level().getDayTime() % DAY_LENGTH;

        return time >= activeEnd && time < activeStart;
    }

    /**
     * 判断当前鸟是否正在睡眠或栖息
     * <p>
     * 通过行为状态控制器读取当前行为状态，
     * 用于避免重复触发睡眠或栖息相关逻辑。
     * </p>
     *
     * @return 当前是否处于睡眠或栖息状态
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSleepingOrRoosting() {
        BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();
        return state == BirdBehaviorState.SLEEPING || state == BirdBehaviorState.ROOSTING;
    }

    public boolean isSleeping() {
        BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();
        return state == BirdBehaviorState.SLEEPING;
    }
}