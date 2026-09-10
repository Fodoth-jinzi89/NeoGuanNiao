package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

import java.util.EnumSet;

public class BirdIdleGoal extends AbstractBirdGoal {

    public BirdIdleGoal(AbstractBirdEntity<?> bird) {
        super(bird);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdIdleGoalController();
    }

}
