package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public class ColumbidCourtshipGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private AbstractColumbidEntity partner;
    private int courtshipTicks;
    private int stepCooldown;

    public ColumbidCourtshipGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.supportsPairBond() || this.columbid.isTame()
                || this.columbid.courtshipCooldown > 0 || !this.columbid.canStartGroundSocialGoal()
                || this.columbid.getRandom().nextInt(220) != 0) {
            return false;
        }

        Optional<AbstractColumbidEntity> currentPartner = this.columbid.pairPartner();
        if (currentPartner.isEmpty() || !this.columbid.hasReciprocalPairWith(currentPartner.get())) {
            return false;
        }

        double distanceSqr = this.columbid.distanceToSqr(currentPartner.get());
        if (distanceSqr < 1.8 || distanceSqr > 81.0) {
            return false;
        }

        this.partner = currentPartner.get();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.courtshipTicks > 0 && this.partner != null && this.partner.isAlive()
                && this.columbid.canStartGroundSocialGoal()
                && this.columbid.hasReciprocalPairWith(this.partner)
                && this.columbid.distanceToSqr(this.partner) < 100.0;
    }

    @Override
    public void start() {
        this.courtshipTicks = 80 + this.columbid.getRandom().nextInt(81);
        this.stepCooldown = 0;
        this.columbid.courtshipCooldown = 760 + this.columbid.getRandom().nextInt(760);
        this.columbid.setBehaviorStateFor(ColumbidBehaviorState.COURTING, Math.min(this.courtshipTicks, 90));
        this.partner.setBehaviorStateFor(ColumbidBehaviorState.COURTING, 45);
        this.columbid.spawnCourtshipParticles(3);
    }

    @Override
    public void tick() {
        --this.courtshipTicks;
        if (this.stepCooldown > 0) {
            --this.stepCooldown;
        }

        this.columbid.getLookControl().setLookAt(this.partner, 26.0F, 26.0F);
        this.partner.getLookControl().setLookAt(this.columbid, 22.0F, 22.0F);

        if (this.courtshipTicks % 42 == 0) {
            this.columbid.spawnCourtshipParticles(2);
        }

        double distanceSqr = this.columbid.distanceToSqr(this.partner);

        if (distanceSqr > 20.0) {
            if (this.stepCooldown <= 0 || this.columbid.getNavigation().isDone()) {
                this.stepCooldown = 12 + this.columbid.getRandom().nextInt(10);
                this.columbid.getNavigation().moveTo(this.partner, 0.68);
            }
        } else if (distanceSqr < 2.2) {
            Vec3 away = this.columbid.position().subtract(this.partner.position()).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() > 1.0E-4) {
                Vec3 target = this.columbid.position().add(away.normalize().scale(1.6));
                this.columbid.getNavigation().moveTo(target.x, target.y, target.z, 0.55);
            }
        } else {
            if (this.stepCooldown <= 0) {
                this.stepCooldown = 18 + this.columbid.getRandom().nextInt(18);
                Vec3 direction = this.columbid.position().subtract(this.partner.position()).multiply(1.0, 0.0, 1.0);
                if (direction.lengthSqr() <= 1.0E-4) {
                    direction = this.columbid.randomHorizontalDirection();
                }
                Vec3 orbit = AbstractColumbidEntity.rotateHorizontal(direction.normalize(),
                        this.columbid.randomSigned(0.9)).scale(2.0 + this.columbid.getRandom().nextDouble() * 0.8);
                Vec3 target = this.columbid.findDryLandingTarget(
                        BlockPos.containing(this.partner.position().add(orbit)), 2
                );
                if (target != null) {
                    this.columbid.getNavigation().moveTo(target.x, target.y, target.z, 0.56);
                } else {
                    this.columbid.getNavigation().stop();
                }
            }
        }
    }

    @Override
    public void stop() {
        this.columbid.getNavigation().stop();
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.COURTING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
        if (this.partner != null && this.partner.getBehaviorState() == ColumbidBehaviorState.COURTING) {
            this.partner.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
        this.partner = null;
    }
}