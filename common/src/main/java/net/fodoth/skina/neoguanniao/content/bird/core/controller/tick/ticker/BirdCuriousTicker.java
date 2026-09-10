package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类好奇状态计时器
 * <p>
 * 负责管理鸟类好奇状态的持续时间。
 * 当计时器大于 0 时，鸟类会处于好奇行为状态，
 * 表现为观察、靠近感兴趣的目标等行为。
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdCuriousTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    public BirdCuriousTicker() {
        super(true, false);
    }
}