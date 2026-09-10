package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public final class BirdWalkAroundGoal extends AbstractBirdGoal {
    public BirdWalkAroundGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdWalkAroundGoalController();
    }
}
