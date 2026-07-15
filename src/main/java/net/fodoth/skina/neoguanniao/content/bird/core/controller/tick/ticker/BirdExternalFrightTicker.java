package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类外部受惊计时器
 * <p>
 * 负责管理鸟类因外部因素（如玩家靠近、攻击等）引起的受惊状态持续时间。
 * 当计时器大于 0 时，鸟类处于受惊状态，可能触发逃跑或警戒行为。
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdExternalFrightTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    /**
     * 创建外部受惊计时器（仅在服务端执行）
     *
     */
    public BirdExternalFrightTicker() {
        super( true, false);
    }
}