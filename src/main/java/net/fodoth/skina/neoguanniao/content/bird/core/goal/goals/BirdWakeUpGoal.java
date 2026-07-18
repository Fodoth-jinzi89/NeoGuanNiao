package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdWakeUpGoal extends AbstractBirdGoal {

    public BirdWakeUpGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdWakeUpGoalController();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return bird().getRoutineController().isSleeping() || super.defaultContinuePredicates();
    }

}
