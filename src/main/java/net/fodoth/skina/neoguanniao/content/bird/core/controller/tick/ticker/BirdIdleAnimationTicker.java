package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类空闲动画计时器
 * <p>
 * 负责管理鸟类空闲动画的播放状态。
 * 该计时器用于控制空闲动画的触发间隔和持续时间，
 * 使鸟类在静止时能够播放随机或循环的空闲动画（如整理羽毛、环顾四周等）。
 * 该计时器在服务端和客户端均执行。
 * </p>
 */
public class BirdIdleAnimationTicker extends AbstractBirdTicker {

    /**
     * 创建空闲动画计时器（在服务端和客户端均执行）
     *
     * @param bird 鸟类实体
     */
    public BirdIdleAnimationTicker(AbstractBirdEntity<?> bird) {
        super(bird);
    }
}