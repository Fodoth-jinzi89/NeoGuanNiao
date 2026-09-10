package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

public abstract class AbstractBirdTimer<T extends AbstractBirdEntity<?>> {

    protected T bird;


    public final void attach(T bird) {
        if (this.bird != null) {
            throw new IllegalStateException(
                    "Ticker is already attached"
            );
        }

        this.bird = bird;

        onAttach();
    }


    protected void onAttach() {
    }


    protected final T bird() {
        if (bird == null) {
            throw new IllegalStateException(
                    "Ticker is not attached"
            );
        }

        return bird;
    }


    public void tick() {
    }


    public void tickClient() {
    }
}