package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

public class BirdUnsafeFloatLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    public BirdUnsafeFloatLoopTicker() {
        super(true, false, true);
    }

    @Override
    protected void reset() {
        super.reset();
        // 设置检查间隔为 20 个游戏刻（约 1 秒），控制检查频率避免过度消耗性能
        setTicks(20);
        processUnsafeFloating();
    }

    private void processUnsafeFloating() {
        T bird = bird();
        // 检查鸟儿是否处于不安全悬浮状态：

        boolean isUnsafeFloating =
                !bird.onGround()
                        && !bird.isFlying()
                        && bird.getBehaviorStateController().getBehaviorState().isUnsafeFloatTickerEnabled()
                        && !bird().getGoalController().getBirdBathUseGoalController().isRunning();

        if (enableLifecycleLog() && bird().getRandom().nextFloat() <= 0.1) {
            NeoGuanNiao.LOGGER.info("[Ticker] UnsafeFloat: Bird unsafe floating check! NotFlying: {}, UnsafeFloatTickerEnabled: {}", !bird.isFlying(), bird.getBehaviorStateController().getBehaviorState().isUnsafeFloatTickerEnabled());
        }

        if (isUnsafeFloating || bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FOLLOWING && bird().getDeltaMovement().length() < 1.0E-4) {
            // 取消无重力状态，防止鸟儿异常悬浮在半空中
            bird.setNoGravity(false);

            // 获取杂项数据，根据鸟儿是否驯服计算冷却时间
            var miscDatum = bird().getBirdData().misc();
            int cooldownTicks = (bird.isTame()
                    ? miscDatum.tameCooldownMin() + bird.getRandom().nextInt(miscDatum.tameCooldownVariance())
                    : miscDatum.wildCooldownMin() + bird.getRandom().nextInt(miscDatum.wildCooldownVariance())
            );

            // 防止滑步
            bird.getFlyingController().setLandingAdjusted(true);

            // 计算着陆等待时间，并限制在合理范围内
            var landingTicks = getTicks() + cooldownTicks;
            bird.getTickController().getTickTimer().getBirdLandingTicker().setTicks(Math.min((int) (landingTicks * 1.2), cooldownTicks * 5));



        }
    }

    @Override
    protected void onReset() {
    }



}