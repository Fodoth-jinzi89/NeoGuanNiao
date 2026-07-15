package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.core.BlockPos;

public class BirdNearbyMusicTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>{

    /**
     * 创建音乐计时器（在服务端执行）
     *
     */
    public BirdNearbyMusicTicker() {
        super(true, false);
    }

    @Override
    protected void run() {
        if (bird instanceof net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity budgerigar) {
            if (ticks-- <= 0) {
                ticks = 18 + budgerigar.getRandom().nextInt(14);
                BlockPos sourcePos = budgerigar.findNearbyJukebox();
                if (sourcePos != null) {
                    budgerigar.triggerMusic(85 + budgerigar.getRandom().nextInt(35));
                    for (net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity budgerigar1 : budgerigar.level().getEntitiesOfClass(
                            net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity.class, budgerigar.getBoundingBox().inflate(10.0))) {
                        if (budgerigar1 != budgerigar && budgerigar1.getRandom().nextFloat() < 0.8F) {
                            budgerigar1.triggerMusic(65 + budgerigar1.getRandom().nextInt(35));
                        }
                    }
                }
            }
        }

    }
}
