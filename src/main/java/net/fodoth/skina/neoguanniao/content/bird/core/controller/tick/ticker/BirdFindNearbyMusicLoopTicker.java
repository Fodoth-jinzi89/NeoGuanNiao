package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.core.BlockPos;

public class BirdFindNearbyMusicLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    /**
     * 创建音乐计时器（在服务端执行）
     * 这个计时器是循环触发的
     *
     */
    public BirdFindNearbyMusicLoopTicker() {
        super(true, false, true);
    }

    @Override
    protected void reset() {
        if (bird() instanceof BudgerigarEntity budgerigar) {
            setTicks(18 + budgerigar.getRandom().nextInt(14));
            BlockPos sourcePos = budgerigar.findNearbyJukebox();
            if (sourcePos != null) {
                budgerigar.triggerMusic(85 + budgerigar.getRandom().nextInt(35));
                for (BudgerigarEntity budgerigar1 : budgerigar.level().getEntitiesOfClass(
                        BudgerigarEntity.class, budgerigar.getBoundingBox().inflate(10.0))) {
                    if (budgerigar1 != budgerigar && budgerigar1.getRandom().nextFloat() < 0.8F) {
                        budgerigar1.triggerMusic(65 + budgerigar1.getRandom().nextInt(35));
                    }
                }
            }

        }
    }

}
