package net.fodoth.skina.neoguanniao.content.bird.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.budgerigar.BudgerigarBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class BudgerigarMusicDanceGoal extends Goal {
    private final BudgerigarEntity budgerigar;

    public BudgerigarMusicDanceGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
    }

    @Override
    public boolean canUse() {
        return this.budgerigar.nearbyMusicTicks() > 0
                && !this.budgerigar.isEating()
                && !this.budgerigar.getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.budgerigar.setBehaviorState(BudgerigarBehaviorState.DANCING);
        this.budgerigar.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.budgerigar.nearbyMusicTicks() > 0
                && !this.budgerigar.isEating()
                && !this.budgerigar.getBehaviorState().isEscape();
    }

    @Override
    public void stop() {
        if (this.budgerigar.getBehaviorState() == BudgerigarBehaviorState.DANCING) {
            this.budgerigar.setBehaviorState(BudgerigarBehaviorState.IDLE);
        }
    }
}