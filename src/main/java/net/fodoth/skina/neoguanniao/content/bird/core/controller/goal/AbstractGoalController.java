package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.AbstractBirdController;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdGoalDatum;

public abstract class AbstractGoalController<T extends AbstractBirdEntity<?>> extends AbstractBirdController<T> {

    public int chance() {
        return 60;
    }

    public boolean canUse() {
        return defaultAdditionalPredicates();
    }

    public boolean canContinue() {
        return canUse();
    }

    public boolean onUse() {
        return true;
    }

    public boolean onContinue() {
        return true;
    }

    public void onStart() {
    }

    public boolean shouldTick() {
        return true;
    }

    public void onTick() {
    }

    public void onReset() {
    }

    public void onStop() {
    }

    protected BirdGoalDatum goalDatum() {
        return bird().getBirdData().goal();
    }

    public boolean defaultAdditionalPredicates() {
        return bird().getRoutineController().isActiveTime()
                && !bird().getEatingController().isEating()
                && !bird().isDancing()
                && !bird().getRoutineController().isSleepingOrRoosting()
                && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }
}
