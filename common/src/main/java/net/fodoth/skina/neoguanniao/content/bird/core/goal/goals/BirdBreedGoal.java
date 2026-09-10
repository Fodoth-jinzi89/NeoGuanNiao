package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdBreedGoal extends AbstractBirdGoal {
    public BirdBreedGoal(AbstractBirdEntity<?> bird) {
        super(bird);
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdBreedGoalController();
    }
}
