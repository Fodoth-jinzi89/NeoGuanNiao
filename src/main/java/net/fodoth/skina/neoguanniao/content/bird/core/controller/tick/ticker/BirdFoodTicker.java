package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类食物效果计时器
 * <p>
 * 负责管理鸟类食物效果的持续时间。
 * 当计时器大于 0 时，表示鸟类仍处于食物效果加成期间，
 * 可能影响饱食度、信任值增长速度或其他与食物相关的属性。
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdFoodTicker extends AbstractBirdTicker {

    /**
     * 创建食物效果计时器（仅在服务端执行）
     *
     * @param bird 鸟类实体
     */
    public BirdFoodTicker(AbstractBirdEntity<?> bird) {
        super(bird, true, false);
    }
}