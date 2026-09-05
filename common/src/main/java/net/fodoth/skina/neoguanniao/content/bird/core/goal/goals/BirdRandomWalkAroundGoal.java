package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

// 暂时没有使用，用IdleGoal吧，以后改个名字做别的
public class BirdRandomWalkAroundGoal extends AbstractBirdGoal {
    public BirdRandomWalkAroundGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdRandomWalkAroundGoalController();
    }

    @Override
    protected void debugStart() {
    }

    @Override
    protected void debugStop() {
    }
}
