package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BirdFollowOwnerGoalController<T extends AbstractBirdEntity<T>> extends AbstractGoalController<T> {
    private LivingEntity owner;

    @Override
    public int chance() {
        return goalDatum().followOwnerChance();
    }


    @Override
    public boolean onUse() {
        if (bird().isTame()) {
            this.owner = bird().getOwner();
            return isOwnerValid(true);
        }
        return false;
    }


    @Override
    public boolean onContinue() {
        return bird().getFlyingController().isFlightInProgress() || isOwnerValid(false);
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);
    }


    @Override
    public void onTick() {
        if (this.owner == null) {
            return;
        }

        bird().getLookControl().setLookAt(this.owner, goalDatum().followOwnerLookYaw(), bird().getMaxHeadXRot());
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);

        double distanceSqr = bird().distanceToSqr(this.owner);

        if (!bird().getFlyingController().isFlightInProgress()) {
            if (distanceSqr > goalDatum().followOwnerStartFlyDistance() && bird().onGround() && !bird().getTickController().getTickTimer().getBirdFlyingTicker().isRunning()) {
                Vec3 target = BirdFlightTargeting.findDryLandingTargetNear(
                        bird(), this.owner.blockPosition(), goalDatum().followOwnerLandingHorizontalRange(), goalDatum().followOwnerLandingVerticalRange());
                if (target != null) {
                    bird().getFlyingController().startShortFlight(target, false);
                }
            }
        }
    }

    @Override
    public void onReset() {
        bird().getNavigation().moveTo(this.owner, goalDatum().followOwnerMoveSpeed());
    }

    @Override
    public void onStop() {
        this.owner = null;
        if (!bird().getFlyingController().isFlightInProgress()
                && bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FOLLOWING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }


    private boolean isOwnerValid(boolean start) {
        if (this.owner == null || !this.owner.isAlive()) {
            return false;
        }
        if (start) {
            return bird().distanceToSqr(this.owner) > goalDatum().followOwnerStartDistance();
        } else {
            return bird().distanceToSqr(this.owner) > goalDatum().followOwnerStopDistance();
        }
    }
}
