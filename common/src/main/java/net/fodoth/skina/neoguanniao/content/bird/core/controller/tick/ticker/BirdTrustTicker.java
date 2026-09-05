package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.minecraft.util.Mth;

/**
 * 鸟类信任值计时器
 * <p>
 * 负责管理鸟类对玩家的信任值。
 * 信任值用于决定鸟类是否能够被驯服，
 * 以及影响鸟类对玩家的亲近程度。
 * 信任值会随时间自然衰减，也可以通过喂食等行为增加。
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdTrustTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    /**
     * 创建信任值计时器（仅在服务端执行）
     *
     */
    public BirdTrustTicker() {
        super(true, false);
    }

    /**
     * 增加鸟的信任值
     * <p>
     * 增加后的值会限制在当前鸟种允许范围内。
     * </p>
     *
     * @param amount 增加的信任值
     */
    public void addTrust(int amount) {
        BirdData birdData = bird().getBirdData();
        int trustLimit = birdData.tame().trustTicksLimit();
        setTicks(Mth.clamp(getTicks() + amount, 0, trustLimit));
    }
}