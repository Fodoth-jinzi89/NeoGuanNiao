package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BudgerigarFollowOwnerGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private final double speed;
    private final float stopDistance;
    private final float startDistance;
    private LivingEntity owner;
    private int repathTicks;

    public BudgerigarFollowOwnerGoal(BudgerigarEntity budgerigar, double speed, float stopDistance, float startDistance) {
        this.budgerigar = budgerigar;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.budgerigar.isTame() && !this.budgerigar.getEatingController().isEating() && !this.budgerigar.isDancing() && !this.budgerigar.getRoutineController().isSleepingOrRoosting()) {
            this.owner = this.budgerigar.getOwner();
            return this.owner != null && this.owner.isAlive()
                    && this.budgerigar.distanceToSqr(this.owner) > this.startDistance * this.startDistance;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && this.owner.isAlive() && !this.budgerigar.getRoutineController().isSleepingOrRoosting() && !this.budgerigar.getEatingController().isEating()
                && !this.budgerigar.isDancing()
                && (this.budgerigar.getFlyingController().isFlightInProgress()
                || this.budgerigar.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.budgerigar.getLookControl().setLookAt(this.owner, 20.0F, this.budgerigar.getMaxHeadXRot());
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);

        double distanceSqr = this.budgerigar.distanceToSqr(this.owner);

        if (!this.budgerigar.getFlyingController().isFlightInProgress()) {
            if (distanceSqr > 49.0 && this.budgerigar.onGround() && this.budgerigar.getTickController().getTickTimer().getBirdFlyingTicker().getTicks() <= 0) {
                Vec3 target = BirdFlightTargeting.findDryLandingTargetNear(
                        this.budgerigar, this.owner.blockPosition(), 5, 10);
                if (target != null) {
                    this.budgerigar.getFlyingController().startShortFlight(target, false);
                    return;
                }
            }

            if (--this.repathTicks <= 0) {
                this.repathTicks = 10;
                this.budgerigar.getNavigation().moveTo(this.owner, this.speed);
            }
        }
    }

    @Override
    public void stop() {
        this.owner = null;
        this.repathTicks = 0;
        if (!this.budgerigar.getFlyingController().isFlightInProgress()
                && this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FOLLOWING) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}