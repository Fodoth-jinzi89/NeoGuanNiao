package net.fodoth.skina.neoguanniao.content.bird.sparrow.goal;

import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightTargeting;
import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SparrowFollowOwnerGoal extends Goal {
    private final SparrowEntity sparrow;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int teleportAttemptTicks;

    public SparrowFollowOwnerGoal(SparrowEntity sparrow, double speedModifier, float stopDistance, float startDistance) {
        this.sparrow = sparrow;
        this.speedModifier = speedModifier;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.sparrow.getOwner();
        if (!this.canFollowOwner(owner)) {
            return false;
        }
        this.owner = owner;
        if (owner != null) {
            return this.sparrow.distanceToSqr(owner) > this.startDistance * this.startDistance;
        } else return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canFollowOwner(this.owner) && this.sparrow.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance;
    }

    @Override
    public void start() {
        this.teleportAttemptTicks = 0;
        this.sparrow.setBehaviorState(SparrowBehaviorState.FOLLOWING_OWNER);
    }

    @Override
    public void tick() {
        if (this.owner == null) return;

        this.sparrow.getLookControl().setLookAt(this.owner, 10.0F, this.sparrow.getMaxHeadXRot());
        this.sparrow.setBehaviorState(SparrowBehaviorState.FOLLOWING_OWNER);
        double distanceSqr = this.sparrow.distanceToSqr(this.owner);

        if (distanceSqr > 64.0 && this.sparrow.onGround() && this.sparrow.flightCooldown <= 0) {
            Vec3 target = BirdFlightTargeting.findDryLandingTargetNear(this.sparrow, this.owner.blockPosition(), 4, 8);
            if (target != null && this.sparrow.startControlledFlight(target, this.sparrow.randomBetween(46, 72), 0.28, false)) {
                return;
            }
        }

        if (distanceSqr > 576.0 && this.sparrow.ownerFollowSuppressedTicks <= 0 && --this.teleportAttemptTicks <= 0) {
            this.teleportAttemptTicks = 20;
            if (this.sparrow.tryTeleportNearOwner(this.owner)) {
                return;
            }
        }

        this.sparrow.getNavigation().moveTo(this.owner, this.speedModifier);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.teleportAttemptTicks = 0;
        if (this.sparrow.getBehaviorState() == SparrowBehaviorState.FOLLOWING_OWNER) {
            this.sparrow.setBehaviorState(SparrowBehaviorState.IDLE);
        }
    }

    private boolean canFollowOwner(LivingEntity owner) {
        if (this.sparrow.isTame() && owner != null && owner.isAlive()
                && this.sparrow.ownerFollowSuppressedTicks <= 0 && !this.sparrow.isControlledFlightActive()
                && this.sparrow.pendingScareTicks <= 0) {
            if (owner instanceof Player player && player.isSpectator()) {
                return false;
            }
            SparrowBehaviorState state = this.sparrow.getBehaviorState();
            return state != SparrowBehaviorState.PERCHING && state != SparrowBehaviorState.ROOSTING && !state.isEscape();
        }
        return false;
    }
}