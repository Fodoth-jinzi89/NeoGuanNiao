package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Keeps non-flying birds moving while their fright state is active. */
public final class BirdEscapeGoal extends Goal {
    private final AbstractBirdEntity<?> bird;

    public BirdEscapeGoal(AbstractBirdEntity<?> bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !bird.canFly()
                && bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FLEEING
                && bird.getFrightController().getGroundEscapeTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        moveToTarget();
    }

    @Override
    public void tick() {
        if (bird.getNavigation().isDone()) {
            bird.getFrightController().continueGroundEscape();
        }
        moveToTarget();
    }

    private void moveToTarget() {
        var target = bird.getFrightController().getGroundEscapeTarget();
        if (target != null) {
            bird.getNavigation().moveTo(target.x, target.y, target.z, 1.5D);
        }
    }
}
