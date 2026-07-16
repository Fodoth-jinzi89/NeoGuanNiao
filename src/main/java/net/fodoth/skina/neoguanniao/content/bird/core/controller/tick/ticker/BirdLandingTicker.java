package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类着陆计时器
 * <p>
 * 负责追踪着陆的冷却时间，一旦落地立即归零。
 * <p>
 * <b>核心作用：</b>
 * <ul>
 *   <li>配合 {@link BirdUnsafeFlyLoopTicker} 防止异常悬空时触发二次飞行</li>
 * </ul>
 *
 * @param <T> 鸟类实体类型，必须继承自 AbstractBirdEntity
 * @see BirdUnsafeFlyLoopTicker 不安全飞行循环处理器
 * @see AbstractBirdTicker 计时器基类
 */
public class BirdLandingTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    /**
     * 构造方法
     * 只在服务端执行，不在客户端执行
     */
    public BirdLandingTicker() {
        super(true, false);
    }

    /**
     * 核心运行逻辑（每个游戏 tick 调用）
     * 执行父类的计时更新逻辑，然后检测鸟儿是否在地面上。
     * 如果在地面上，立即将计时器归零。
     * 会触发 {@link #onExpire()}
     */
    @Override
    protected void run() {
        super.run();

        T bird = bird();

        boolean onGround = bird.onGround();
        boolean isSleepingOrRoosting = bird().getRoutineController().isSleepingOrRoosting();

        if (onGround || isSleepingOrRoosting) {
            if (enableLifecycleLog()) {
                NeoGuanNiao.LOGGER.info("[Ticker] Landing: Bird on ground = {}, sleeping or roosting = {}", onGround, isSleepingOrRoosting);
            }
            setTicks(0);
            bird.getTickController().getTickTimer().getBirdBehaviorStateTicker().setTicks(5);

            return;
        }
        bird().getFlyingController().processLanding();
    }
}