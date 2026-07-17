package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

public class BirdFrightLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    public BirdFrightLoopTicker() {
        super(true, false, true);
    }

    @Override
    protected void reset() {
        super.reset();
        var frightData = bird().getBirdData().fright();
        setTicks(frightData.frightCheckTicks() + bird().getRandom().nextInt(frightData.frightCheckTicksVariance()));
    }

    @Override
    protected void onReset() {
        if (!bird().getFrightController().shouldFlee()) {
            return;
        }
        var frightData = bird().getBirdData().fright();
        bird().getFrightController().frightenFrom(frightData.frightenFromTicks() + bird().getRandom().nextInt(frightData.frightenFromTicksVariance()));
    }
}
