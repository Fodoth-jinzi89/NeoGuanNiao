package net.fodoth.skina.neoguanniao.content.bird.core.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdGoalDatum;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class AbstractBirdGoal extends Goal {

    private final AbstractBirdEntity<?> bird;
    // 只有这个 ticks 应该在 goal 内部维护
    private int repathTicks;
    // 越大触发概率越低
    // 想要依据 BirdData 调整的话还得带泛型
    private final int chance;
    private final int maxRepathTicks;

    public AbstractBirdGoal(AbstractBirdEntity<?> bird) {
        this(bird, 60, 10);
    }

    public AbstractBirdGoal(AbstractBirdEntity<?> bird, int chance) {
        this(bird, chance, 10);
    }

    public AbstractBirdGoal(AbstractBirdEntity<?> bird, int chance, int maxRepathTicks) {
        this.bird = bird;
        this.chance = chance;
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


    protected void onStart() {}

    protected void onTick() {}

    protected void onReset() {}

    protected void onStop() {}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean defaultUsePredicates() {
        return bird().isAddedToLevel() &&
                !bird().isDeadOrDying() &&
                !bird().isNoAi() &&
                !bird().isPassenger() &&
                !bird().isFullyFrozen() &&
                !bird().isLeashed() &&
                !bird().isRemoved() &&
                bird().getRandom().nextInt(getChance()) == 0;
    }

    // 可选调用
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean defaultAdditionalPredicates() {
        return bird().getRoutineController().isActiveTime()
                && !bird().getEatingController().isEating()
                && !bird().isDancing()
                && !bird().getRoutineController().isSleepingOrRoosting()
                && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean defaultContinuePredicates() {
        return !bird().getEatingController().isEating() &&
                !bird().isDancing() &&
                !bird().getRoutineController().isSleepingOrRoosting() && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }

    protected boolean usePredicates() {
        return true;
    };

    protected boolean continuePredicates() {
        return true;
    };

    public AbstractBirdEntity<?> bird() {
        return bird;
    }

    public int getChance() {
        return chance;
    }

    public int getMaxRepathTicks() {
        return maxRepathTicks;
    }

    protected BirdGoalDatum goalDatum() {
        return bird().getBirdData().goal();
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