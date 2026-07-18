package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

public class BirdMusicDanceGoalController<T extends AbstractBirdEntity<T>> extends AbstractGoalController<T> {

    @Override
    public int chance() {
        return goalDatum().musicDanceChance();
    }

    @Override
    public boolean canUse() {
        return super.canUse() && bird().getTickController().getTickTimer().getBirdMusicTicker().isRunning();
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.DANCING);
        bird().getNavigation().stop();
    }

    @Override
    public boolean defaultAdditionalPredicates() {
        return bird().isDancing() || super.defaultAdditionalPredicates();
    }

    @Override
    public void onStop() {
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }

}
