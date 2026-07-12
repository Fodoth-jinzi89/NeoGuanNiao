package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ColumbidFollowOwnerGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private final double speed;
    private final float stopDistance;
    private final float startDistance;
    private LivingEntity owner;
    private int repathTicks;

    public ColumbidFollowOwnerGoal(AbstractColumbidEntity columbid, double speed, float stopDistance, float startDistance) {
        this.columbid = columbid;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.columbid.isTame() && !this.columbid.isControlledFlightActive()) {
            this.owner = this.columbid.getOwner();
            return this.owner != null && this.owner.isAlive()
                    && this.columbid.distanceToSqr(this.owner) > this.startDistance * this.startDistance;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && this.owner.isAlive() && !this.columbid.isControlledFlightActive()
                && this.columbid.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance
                && !this.columbid.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.columbid.setBehaviorState(ColumbidBehaviorState.FOLLOWING_OWNER);
    }

    @Override
    public void tick() {
        this.columbid.getLookControl().setLookAt(this.owner, 10.0F, this.columbid.getMaxHeadXRot());
        this.columbid.setBehaviorState(ColumbidBehaviorState.FOLLOWING_OWNER);

        if (this.columbid.distanceToSqr(this.owner) > 64.0 && this.columbid.onGround()
                && this.columbid.flightCooldown <= 0) {
            BlockPos ownerPos = this.owner.blockPosition();
            Vec3 target = this.columbid.findDryLandingTargetNear(ownerPos, 5, 10);
            if (target != null) {
                this.columbid.startControlledFlight(target, 120, 0.38, false, true);
                return;
            }
        }

        if (--this.repathTicks <= 0) {
            this.repathTicks = 12;
            this.columbid.getNavigation().moveTo(this.owner, this.speed);
        }
    }

    @Override
    public void stop() {
        this.owner = null;
        this.columbid.getNavigation().stop();
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.FOLLOWING_OWNER) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }
}