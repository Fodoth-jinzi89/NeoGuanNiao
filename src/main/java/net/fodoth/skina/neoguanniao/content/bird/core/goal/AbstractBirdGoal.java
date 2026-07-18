package net.fodoth.skina.neoguanniao.content.bird.core.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.AbstractGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdGoalDatum;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class AbstractBirdGoal extends Goal {

    private final AbstractBirdEntity<?> bird;
    // 只有这个 ticks 应该在 goal 内部维护
    private int repathTicks;
    private final int maxRepathTicks;

    public AbstractBirdGoal(AbstractBirdEntity<?> bird) {
        this(bird, 10);
    }


    public AbstractBirdGoal(AbstractBirdEntity<?> bird, int maxRepathTicks) {
        this.bird = bird;
        this.maxRepathTicks = maxRepathTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public final boolean canUse() {
        return defaultUsePredicates() && usePredicates();
    }

    @Override
    public final boolean canContinueToUse() {
        return defaultContinuePredicates() && continuePredicates();
    }

    @Override
    public final void start() {
        this.repathTicks = 0;
        debugStart();
        onStart();
    }

    protected void debugStart() {
        var debugTicker = bird().getTickController().getTickTimer().getDebugLoopTicker();
        if (debugTicker.enableLifecycleLog()) {
            debugTicker.debugGoalStart(this.getClass().getSimpleName());
        }
    }

    @Override
    public final void tick() {
        onTick();
        if (--this.repathTicks <= 0) {
            debugReset();
            reset();
            onReset();
        }
    }

    protected void reset() {
        this.repathTicks = maxRepathTicks;
    }

    @Override
    public final void stop() {
        this.repathTicks = 0;
        debugStop();
        onStop();
    }

    protected void debugStop() {
        var debugTicker = bird().getTickController().getTickTimer().getDebugLoopTicker();
        if (debugTicker.enableLifecycleLog()) {
            debugTicker.debugGoalStop(this.getClass().getSimpleName());
        }
    }

    protected void debugReset() {
        // goals are all equivalent loop tickers (with priority)
    }


    protected void onStart() {
        individualGoalController().onStart();
    }

    protected void onTick() {
        individualGoalController().onTick();
    }

    protected void onReset() {
        individualGoalController().onReset();
    }

    protected void onStop() {
        individualGoalController().onStop();
    }

    // 子类覆写需要保留在goal中
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean defaultUsePredicates() {
        return bird().isAddedToLevel() &&
                !bird().isDeadOrDying() &&
                !bird().isNoAi() &&
                !bird().isPassenger() &&
                !bird().isFullyFrozen() &&
                !bird().isLeashed() &&
                !bird().isRemoved() &&
                bird().getRandom().nextInt(individualGoalController().chance()) == 0;
    }

    // 子类覆写需要保留在goal中
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean defaultContinuePredicates() {
        return !bird().getEatingController().isEating() &&
                !bird().isDancing() &&
                !bird().getRoutineController().isSleepingOrRoosting() && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }

    // 子类一般无需覆写
    protected boolean usePredicates() {
        if (individualGoalController().canUse()) {
            return individualGoalController().onUse();
        } else return false;
    };

    // 子类一般无需覆写
    protected boolean continuePredicates() {
        if (individualGoalController().canContinue()) {
            return individualGoalController().onContinue();
        } else return false;
    };

    public AbstractBirdEntity<?> bird() {
        return bird;
    }

    public int getMaxRepathTicks() {
        return maxRepathTicks;
    }

    protected BirdGoalDatum goalDatum() {
        return bird().getBirdData().goal();
    }

    protected BirdGoalController<?> goalController() {
        return bird().getGoalController();
    }

    protected AbstractGoalController<?> individualGoalController() {
        return null;
    }

    protected int getRepathTicks() {
        return repathTicks;
    }

    protected void setRepathTicks(int repathTicks) {
        this.repathTicks = repathTicks;
    }

    protected void setRepathTicksWithVariance(int repathTicks, int variance) {
        setRepathTicks(repathTicks + bird().getRandom().nextInt(variance));
    }
}