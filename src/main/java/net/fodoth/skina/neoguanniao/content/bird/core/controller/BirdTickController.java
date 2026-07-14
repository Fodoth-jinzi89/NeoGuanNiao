package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.BirdTickTimer;

/**
 * 鸟类 Tick 生命周期控制器
 * <p>
 * 负责维护鸟实体运行过程中各种基于 Tick 的临时状态，
 * 并驱动鸟类核心行为逻辑，包括：
 * <ul>
 *     <li>行为状态锁定与恢复</li>
 *     <li>进食计时</li>
 *     <li>驯服后的庆祝行为</li>
 *     <li>落水逃离行为</li>
 *     <li>受惊延迟处理</li>
 *     <li>飞行控制与巡航</li>
 *     <li>默认行为状态修正</li>
 *     <li>地面移动朝向同步</li>
 * </ul>
 * 该控制器主要负责封装时间驱动逻辑，
 * 不直接定义具体行为规则，行为选择由各个 Ticker 负责。
 * </p>
 */
public class BirdTickController {

    /**
     * 行为状态锁定剩余 Tick 数
     */
    public int behaviorStateLockTicks;

    /**
     * 鸟类 Tick 计时器，管理所有基于 Tick 的计时器
     */
    private final BirdTickTimer tickTimer;

    /**
     * 创建鸟类 Tick 生命周期控制器
     *
     * @param entity 鸟类实体
     */
    public BirdTickController(AbstractBirdEntity<?> entity) {
        this.tickTimer = new BirdTickTimer(entity);
    }

    /**
     * 执行服务端 Tick 更新
     * <p>
     * 驱动所有服务端计时器更新。
     * </p>
     */
    public void tick() {
        tickTimer.runTick();
    }

    /**
     * 执行客户端 Tick 更新
     * <p>
     * 驱动所有客户端计时器更新。
     * </p>
     */
    public void tickClient() {
        tickTimer.runClientTick();
    }

    /**
     * 获取鸟类 Tick 计时器
     *
     * @return 鸟类 Tick 计时器实例
     */
    public BirdTickTimer getTickTimer() {
        return tickTimer;
    }
}