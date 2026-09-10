package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

import java.util.EnumSet;

public class BirdFlockGoal extends AbstractBirdGoal {

    public BirdFlockGoal(AbstractBirdEntity<?> bird) {
        super(bird);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    protected AbstractGoalController<?> individualGoalController() {
        return goalController().getBirdFlockGoalController();
    }

}
