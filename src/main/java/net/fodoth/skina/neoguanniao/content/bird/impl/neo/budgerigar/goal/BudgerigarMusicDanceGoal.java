package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class BudgerigarMusicDanceGoal extends Goal {
    private final BudgerigarEntity budgerigar;

    public BudgerigarMusicDanceGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
    }

    @Override
    public boolean canUse() {
        return this.budgerigar.getTickController().getTickTimer().getBirdNearbyMusicTicker().getTicks() > 0
                && !this.budgerigar.getEatingController().isEating()
                && !this.budgerigar.getBehaviorStateController().getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.DANCING);
        this.budgerigar.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.budgerigar.getTickController().getTickTimer().getBirdNearbyMusicTicker().getTicks() > 0
                && !this.budgerigar.getEatingController().isEating()
                && !this.budgerigar.getBehaviorStateController().getBehaviorState().isEscape();
    }

    @Override
    public void stop() {
        if (this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}