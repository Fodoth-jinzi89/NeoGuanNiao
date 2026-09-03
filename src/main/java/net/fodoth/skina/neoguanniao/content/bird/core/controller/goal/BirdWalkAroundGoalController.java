package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public final class BirdWalkAroundGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
    private Vec3 target;

    @Override
    public int chance() {
        return goalDatum().randomWalkAroundChance();
    }

    @Override
    public boolean canUse() {
        BirdBehaviorState state = bird().getBehaviorStateController().getBehaviorState();
        return bird().onGround()
                && !bird().getFlyingController().isFlightInProgress()
                && (state == BirdBehaviorState.IDLE || state == BirdBehaviorState.SENTINEL)
                && !bird().hasControllingPassenger()
                && super.canUse();
    }

    @Override
    public boolean onUse() {
        target = LandRandomPos.getPos(bird(), goalDatum().randomWalkAroundHorizontalRange(),
                goalDatum().randomWalkAroundVerticalRange());
        return target != null && target.y <= bird().getY() + 0.5D;
    }

    @Override
    public boolean canContinue() {
        return target != null
                && !bird().getFlyingController().isFlightInProgress()
                && !bird().getNavigation().isDone()
                && !bird().hasControllingPassenger()
                && defaultAdditionalPredicates();
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
        bird().getNavigation().moveTo(target.x, target.y, target.z,
                goalDatum().randomWalkAroundSpeedModifier());
    }

    @Override
    public void onStop() {
        target = null;
        bird().getNavigation().stop();
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}
