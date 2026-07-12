package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ColumbidFlockOrPairGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private AbstractColumbidEntity socialTarget;
    private Vec3 target;
    private int moveTicks;
    private boolean followingPartner;

    public ColumbidFlockOrPairGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.canStartGroundSocialGoal() || this.columbid.getRandom().nextInt(34) != 0) {
            return false;
        }

        Optional<AbstractColumbidEntity> partner = this.columbid.pairPartner();
        if (partner.isPresent()) {
            double distance = this.columbid.distanceToSqr(partner.get());
            if (distance > 18.0 && distance < 196.0) {
                this.socialTarget = partner.get();
                this.followingPartner = true;
                this.target = partner.get().position().add(
                        this.columbid.randomSigned(1.2), 0, this.columbid.randomSigned(1.2)
                );
                return true;
            }
        }

        List<AbstractColumbidEntity> flock = this.columbid.level().getEntitiesOfClass(
                AbstractColumbidEntity.class,
                this.columbid.getBoundingBox().inflate(12.0),
                other -> other.getClass() == this.columbid.getClass() && other != this.columbid && other.isAlive()
        );

        if (flock.isEmpty()) {
            return false;
        }

        AbstractColumbidEntity other = flock.get(this.columbid.getRandom().nextInt(flock.size()));
        Vec3 away = this.columbid.position().subtract(other.position()).multiply(1.0, 0.0, 1.0);

        if (this.columbid.distanceToSqr(other) < 2.6 && away.lengthSqr() > 1.0E-4) {
            this.socialTarget = other;
            this.followingPartner = false;
            this.target = this.columbid.position().add(away.normalize().scale(2.6));
            return true;
        } else if (this.columbid.distanceToSqr(other) > 36.0) {
            this.socialTarget = other;
            this.followingPartner = false;
            this.target = other.position();
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.moveTicks > 0 && this.target != null && this.socialTarget != null
                && this.socialTarget.isAlive() && !this.columbid.isControlledFlightActive()
                && this.columbid.canStartGroundSocialGoal()
                && (!this.followingPartner || this.columbid.hasReciprocalPairWith(this.socialTarget));
    }

    @Override
    public void start() {
        this.moveTicks = 35 + this.columbid.getRandom().nextInt(35);
        this.columbid.setBehaviorState(ColumbidBehaviorState.PAIR_FOLLOWING);

        if (this.columbid.onGround() && this.columbid.flightCooldown <= 0 && this.target != null
                && this.columbid.distanceToSqr(this.target) > 64.0) {
            Vec3 landing = this.columbid.findDryLandingTargetNear(
                    BlockPos.containing(this.target), 4, 7
            );
            if (landing != null && this.columbid.startControlledFlight(
                    landing, 100 + this.columbid.getRandom().nextInt(45), 0.38, false, true)) {
                return;
            }
        }

        this.columbid.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, 0.78);
    }

    @Override
    public void tick() {
        --this.moveTicks;
        this.columbid.getLookControl().setLookAt(this.socialTarget, 18.0F, 18.0F);

        if (this.followingPartner && this.moveTicks % 18 == 0) {
            double distanceSqr = this.columbid.distanceToSqr(this.socialTarget);
            if (distanceSqr > 20.0) {
                this.target = this.socialTarget.position().add(
                        this.columbid.randomSigned(1.4), 0, this.columbid.randomSigned(1.4)
                );
                this.columbid.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, 0.78);
            } else if (distanceSqr < 2.4) {
                Vec3 away = this.columbid.position().subtract(this.socialTarget.position()).multiply(1.0, 0.0, 1.0);
                if (away.lengthSqr() > 1.0E-4) {
                    this.target = this.columbid.position().add(away.normalize().scale(1.8));
                    this.columbid.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, 0.62);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.socialTarget = null;
        this.target = null;
        this.followingPartner = false;
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.PAIR_FOLLOWING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }
}