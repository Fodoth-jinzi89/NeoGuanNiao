package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdRandomLookAroundGoal extends AbstractBirdGoal {

    public BirdRandomLookAroundGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdRandomLookAroundGoalController();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    protected void reset() {
        setRepathTicksWithVariance(goalDatum().randomLookAroundTicks(), goalDatum().randomLookAroundTicksVariance());
    }

}
