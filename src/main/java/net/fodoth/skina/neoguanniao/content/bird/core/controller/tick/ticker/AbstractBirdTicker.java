package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类 Tick 计时器抽象基类
 *
 * <p>
 * 提供所有鸟类计时器的通用功能，包括：
 * <ul>
 *     <li>计时器 Tick 计数管理</li>
 *     <li>服务端/客户端 Tick 控制</li>
 *     <li>运行时逻辑钩子</li>
 *     <li>实体绑定生命周期</li>
 * </ul>
 * </p>
 */
public abstract class AbstractBirdTicker<T extends AbstractBirdEntity<T>> {

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



    protected final boolean isLoopTicker;


    /**
     * 关联的鸟类实体
     */
    protected T bird;


    /**
     * 默认构造
     *
     * <p>
     * 服务端和客户端均执行 Tick。
     * </p>
     */
    public AbstractBirdTicker() {
        this(true, true);
    }


    /**
     * 创建计时器
     *
     * @param shouldTickCommon 是否执行服务端 Tick
     * @param shouldTickClient 是否执行客户端 Tick
     */
    public AbstractBirdTicker(
            boolean shouldTickCommon,
            boolean shouldTickClient
    ) {
        this(shouldTickCommon, shouldTickClient, false);
    }

    /**
     * 创建计时器
     *
     * @param shouldTickCommon 是否执行服务端 Tick
     * @param shouldTickClient 是否执行客户端 Tick
     * @param isLoopTicker 是否是循环计时器（归零后会重置计时）
     */
    public AbstractBirdTicker(
            boolean shouldTickCommon,
            boolean shouldTickClient,
            boolean isLoopTicker
    ) {
        this.shouldTickCommon = shouldTickCommon;
        this.shouldTickClient = shouldTickClient;
        this.isLoopTicker = isLoopTicker;
    }


    /**
     * 绑定鸟类实体
     *
     * @param bird 鸟类实体
     */
    public final void attach(T bird) {

        if (this.bird != null) {
            throw new IllegalStateException(
                    "Ticker is already attached"
            );
        }

        this.bird = bird;

        onAttach();
    }


    /**
     * 绑定完成后的初始化回调
     *
     * <p>
     * 子类可以覆写此方法进行依赖实体后的初始化。
     * </p>
     */
    protected void onAttach() {
    }


    /**
     * 获取关联鸟类实体
     *
     * @return 鸟类实体
     */
    protected final T bird() {

        if (bird == null) {
            throw new IllegalStateException(
                    "Ticker is not attached"
            );
        }

        return bird;
    }


    /**
     * 获取当前 Tick 数
     */
    public int getTicks() {
        return ticks;
    }


    /**
     * 设置 Tick 数
     */
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }


    /**
     * 客户端 Tick 开关
     */
    public boolean shouldTickClient() {
        return shouldTickClient;
    }


    /**
     * 服务端 Tick 开关
     */
    public boolean shouldTickCommon() {
        return shouldTickCommon;
    }

    public boolean isLoopTicker() {
        return isLoopTicker;
    }


    /**
     * 服务端 Tick
     */
    public void tick() {

        if (ticks <= 0 && isLoopTicker) {
            reset();
        }

        if (shouldTickCommon && ticks > 0) {
            --ticks;
        }


        run();
    }


    /**
     * 客户端 Tick
     */
    public void tickClient() {

        if (ticks <= 0 && isLoopTicker) {
            reset();
        }

        if (shouldTickClient && ticks > 0) {
            --ticks;
        }

        runClient();
    }


    /**
     * 服务端逻辑
     */
    protected void run() {
    }


    /**
     * 客户端逻辑
     */
    protected void runClient() {
        run();
    }


    protected void reset() {

    }
}