package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类 Tick 计时器抽象基类
 * <p>
 * 提供所有鸟类计时器的通用功能，包括：
 * <ul>
 *     <li>计时器 Tick 计数管理</li>
 *     <li>服务端/客户端 Tick 控制</li>
 *     <li>可扩展的运行时逻辑钩子</li>
 * </ul>
 * </p>
 */
public abstract class AbstractBirdTicker {

    /**
     * 当前计时器剩余 Tick 数
     */
    protected int ticks;

    /**
     * 是否在服务端执行 Tick 更新
     */
    protected final boolean shouldTickCommon;

    /**
     * 是否在客户端执行 Tick 更新
     */
    protected final boolean shouldTickClient;

    /**
     * 关联的鸟类实体
     */
    protected final AbstractBirdEntity<?> bird;

    /**
     * 创建计时器（默认在服务端和客户端均执行 Tick）
     *
     * @param bird 鸟类实体
     */
    public AbstractBirdTicker(AbstractBirdEntity<?> bird) {
        this.bird = bird;
        this.shouldTickCommon = true;
        this.shouldTickClient = true;
    }

    /**
     * 创建计时器（可控制服务端/客户端执行）
     *
     * @param bird            鸟类实体
     * @param shouldTickCommon 是否在服务端执行 Tick
     * @param shouldTickClient 是否在客户端执行 Tick
     */
    public AbstractBirdTicker(AbstractBirdEntity<?> bird, boolean shouldTickCommon, boolean shouldTickClient) {
        this.bird = bird;
        this.shouldTickCommon = shouldTickCommon;
        this.shouldTickClient = shouldTickClient;
    }

    /**
     * 获取关联的鸟类实体
     *
     * @return 鸟类实体
     */
    public AbstractBirdEntity<?> getBird() {
        return bird;
    }

    /**
     * 获取当前计时器剩余 Tick 数
     *
     * @return 剩余 Tick 数
     */
    public int getTicks() {
        return ticks;
    }

    /**
     * 设置计时器 Tick 数
     *
     * @param ticks 要设置的 Tick 数
     */
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    /**
     * 判断是否在客户端执行 Tick
     *
     * @return 如果客户端执行返回 true
     */
    public boolean shouldTickClient() {
        return shouldTickClient;
    }

    /**
     * 判断是否在服务端执行 Tick
     *
     * @return 如果服务端执行返回 true
     */
    public boolean shouldTickCommon() {
        return shouldTickCommon;
    }

    /**
     * 执行服务端 Tick 更新
     * <p>
     * 减少计时器计数并调用自定义逻辑。
     * </p>
     */
    public void tick() {
        if (shouldTickCommon && ticks > 0) {
            --ticks;
        }
        run();
    }

    /**
     * 执行客户端 Tick 更新
     * <p>
     * 减少计时器计数并调用客户端自定义逻辑。
     * </p>
     */
    public void tickClient() {
        if (shouldTickClient && ticks > 0) {
            --ticks;
        }
        runClient();
    }

    /**
     * 服务端自定义逻辑钩子
     * <p>
     * 子类可重写此方法实现特定的服务端 Tick 逻辑。
     * </p>
     */
    protected void run() {
        // 默认无操作
    }

    /**
     * 客户端自定义逻辑钩子
     * <p>
     * 子类可重写此方法实现特定的客户端 Tick 逻辑。
     * 默认实现会调用 {@link #run()} 以保持一致性。
     * </p>
     */
    protected void runClient() {
        run();
    }
}