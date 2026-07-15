package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类驯服后行为切换计时器
 * <p>
 * 负责管理驯服成功后行为切换的计时。
 * 该计时器用于控制驯服庆祝期间不同行为阶段之间的切换时机，
 * 使驯服后的行为表现更加丰富和自然。
 * 该计时器在服务端和客户端均执行。
 * </p>
 */
public class BirdPostTameActionSwapTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    /**
     * 创建驯服后行为切换计时器（在服务端和客户端均执行）
     *
     */
    public BirdPostTameActionSwapTicker() {
        super();
    }
}