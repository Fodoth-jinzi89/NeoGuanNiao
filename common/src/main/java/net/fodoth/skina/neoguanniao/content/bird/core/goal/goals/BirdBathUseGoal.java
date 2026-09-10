package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.BirdBathUseGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdBathUseGoal extends AbstractBirdGoal {

    public BirdBathUseGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdBathUseGoalController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetTicks();
    }

    @Override
    protected void onReset() {
        super.onReset();
        resetTicks();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return bird().getBehaviorStateController().getBehaviorState().isEscape() || bird().getEatingController().isForagingOrEating() || super.defaultContinuePredicates();
    }

    private void resetTicks() {
        if (individualGoalController() instanceof BirdBathUseGoalController<?> birdBathUseGoalController) {
            if (birdBathUseGoalController.bathExists()) {
                setRepathTicksWithVariance(goalDatum().bathUseStartTicks(), goalDatum().bathUseStartTicksVariance());
            }
        }
    }

}
