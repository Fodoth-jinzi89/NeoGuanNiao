package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

public class BirdMusicTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>  {
    public BirdMusicTicker() {
        super(true, false);
    }
}
