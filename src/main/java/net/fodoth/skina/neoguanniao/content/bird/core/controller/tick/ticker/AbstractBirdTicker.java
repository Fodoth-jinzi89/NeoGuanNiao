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
     * <p>
     * 请使用 {@link #getTicks()} 和 {@link #setTicks(int)} 方法访问此字段，不要直接访问。
     * </p>
     */
    private int ticks;

    /**
     * 关联的鸟类实体
     */
    // Lazy attach
    private T bird;

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
     *
     * @param ticks 要设置的 tick 数量，若在{@link #run()}中设置为 0 将触发一次 {@link #onExpire()}
     * @throws IllegalArgumentException 如果 ticks 为负数
     */
    public void setTicks(int ticks) {
        // 参数验证
        if (ticks < 0) {
            throw new IllegalArgumentException("Ticks cannot be negative: " + ticks);
        }

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

        if (!shouldTickCommon) {
            return;
        }

        // 循环计时器归零时重置
        if (isLoopTicker && ticks <= 0) {
            reset();
        }

        // 计时器递减并执行
        if (ticks > 0) {
            --ticks;
            run();
            if (ticks <= 0) {
                onExpire();
            }
        }

    }


    /**
     * 客户端 Tick
     */
    public void tickClient() {

        if (!shouldTickClient) {
            return;
        }

        // 循环计时器归零时重置
        if (isLoopTicker && ticks <= 0) {
            resetClient();
        }

        // 计时器递减并执行
        if (ticks > 0) {
            --ticks;
            runClient();
            if (ticks <= 0) {
                onExpireClient();
            }
        }
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

    protected void resetClient() {
        reset();
    }

    protected void onExpire() {
    }

    protected void onExpireClient() {
        onExpire();
    }
}