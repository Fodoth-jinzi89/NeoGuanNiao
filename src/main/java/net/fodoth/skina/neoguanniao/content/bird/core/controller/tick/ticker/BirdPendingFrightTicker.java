package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.minecraft.world.phys.Vec3;

/**
 * 鸟类待处理受惊计时器
 * <p>
 * 负责管理延迟触发的受惊行为。
 * 当鸟类受到惊吓但需要延迟响应时（如攻击者距离较远或需要先完成当前行为），
 * 该计时器会等待指定时间后再执行逃跑行为。
 * 计时器运行期间，鸟类会持续注视惊吓来源。
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdPendingFrightTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    /**
     * 待处理受惊的持续时间
     */
    public int pendingFrightDuration;

    /**
     * 创建待处理受惊计时器（仅在服务端执行）
     *
     */
    public BirdPendingFrightTicker() {
        super( true, false);
    }

    /**
     * 处理延迟触发的惊吓行为
     * <p>
     * 等待惊吓计时结束后，根据记录的位置执行逃跑行为。
     * </p>
     */
    @Override
    protected void run() {
        // 计时器归零时不处理
        if (ticks <= 0) {
            return;
        }

        var frightController = bird.getFrightController();
        BirdData birdData = bird.getbirdData();

        // 停止导航移动
        bird.getNavigation().stop();

        // 计时器仍在运行时，持续注视惊吓来源
        if (ticks > 0) {
            Vec3 sourcePos = frightController.pendingFrightSource;
            if (sourcePos != null) {
                double lookX = sourcePos.x;
                double lookY = sourcePos.y + birdData.fright().pendingFrightLookYOffset();
                double lookZ = sourcePos.z;
                float lookSpeed = birdData.fright().pendingFrightLookSpeed();
                bird.getLookControl().setLookAt(lookX, lookY, lookZ, lookSpeed, lookSpeed);
            }
            return;
        }

        // 计时器归零，执行实际受惊行为
        Vec3 sourcePos = frightController.pendingFrightSource != null
                ? frightController.pendingFrightSource
                : bird.position();

        // 确定受惊持续时间
        int minDuration = birdData.fright().pendingFrightMinDuration();
        int duration = Math.max(minDuration, pendingFrightDuration);

        // 清理待处理状态并触发受惊
        frightController.pendingFrightSource = null;
        pendingFrightDuration = 0;
        frightController.frightenFrom(sourcePos, duration);
    }
}