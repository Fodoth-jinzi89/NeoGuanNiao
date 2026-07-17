package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdWakeUpGoal extends AbstractBirdGoal {

    public BirdWakeUpGoal(AbstractBirdEntity<?> bird) {
        super(bird, 5);
    }

    @Override
    protected boolean usePredicates() {
        return bird().getRoutineController().isSleeping() && bird().getRoutineController().isActiveTime();
    }

    @Override
    protected boolean continuePredicates() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return bird().getRoutineController().isSleeping() || super.defaultContinuePredicates();
    }

    @Override
    protected void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        bird().getNavigation().stop();
    }
}
