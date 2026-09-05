package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

public class BirdFrightTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>  {

    public BirdFrightTicker() {
        super(true,false);
    }

    @Override
    protected void onExpire() {
        bird().getFrightController().setFrightSource(null);
    }
}
