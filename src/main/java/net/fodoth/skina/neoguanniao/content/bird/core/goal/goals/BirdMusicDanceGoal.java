package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

import java.util.EnumSet;

public class BirdMusicDanceGoal extends AbstractBirdGoal {

    public BirdMusicDanceGoal(AbstractBirdEntity<?> bird) {
        super(bird, 5);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    protected boolean usePredicates() {
        return defaultAdditionalPredicates() && bird().getTickController().getTickTimer().getBirdMusicTicker().isRunning();
    }

    @Override
    protected boolean continuePredicates() {
        return usePredicates();
    }

    @Override
    protected void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.DANCING);
        bird().getNavigation().stop();
    }

    @Override
    protected boolean defaultAdditionalPredicates() {
        return bird().isDancing() || super.defaultAdditionalPredicates();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return bird().isDancing() || super.defaultContinuePredicates();
    }

    @Override
    protected void onStop() {
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }


}
