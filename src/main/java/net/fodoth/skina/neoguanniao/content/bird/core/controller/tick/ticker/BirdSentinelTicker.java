package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;

public class BirdSentinelTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    public BirdSentinelTicker() {
        super(true, true);
    }

    @Override
    protected void run() {
        if (!defaultAdditionalPredicates()) {
            setTicks(0);
            return;
        }

        var goalDatum = bird().getBirdData().goal();
        if (getTicks() % goalDatum.sentinelLookAroundInterval() == 0) {
            // 定期环顾四周
            float yaw = bird().getYRot() + goalDatum.sentinelLookYawVariance() * (bird().getRandom().nextBoolean() ? 1 : -1);
            bird().setYRot(yaw);
            bird().yBodyRot = yaw;
        }
    }

    @Override
    protected void onExpire() {
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SENTINEL) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }

    @Override
    protected void onSet(int ticksOld, int ticksNew) {
    }


    private boolean defaultAdditionalPredicates() {
        return bird().getRoutineController().isActiveTime()
                && !bird().getEatingController().isEating()
                && !bird().isDancing()
                && !bird().getRoutineController().isSleepingOrRoosting()
                && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }
}
