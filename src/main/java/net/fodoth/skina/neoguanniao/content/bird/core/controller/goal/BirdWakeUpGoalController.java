package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

public class BirdWakeUpGoalController<T extends AbstractBirdEntity<T>> extends AbstractGoalController<T>  {

    @Override
    public int chance() {
        return goalDatum().wakeUpChance();
    }

    @Override
    public boolean canUse() {
        return bird().getRoutineController().isSleeping() && bird().getRoutineController().isActiveTime();
    }

    @Override
    public boolean canContinue() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        bird().getNavigation().stop();
    }
}
