package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public class ColumbidIdleGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private Vec3 strollTarget;
    private int idleTicks;

    public ColumbidIdleGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.columbid.canStartGroundSocialGoal() && this.columbid.getNavigation().isDone()
                && this.columbid.getRandom().nextInt(18) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.idleTicks > 0 && !this.columbid.isControlledFlightActive();
    }

    @Override
    public void start() {
        this.idleTicks = 45 + this.columbid.getRandom().nextInt(85);
        int roll = this.columbid.getRandom().nextInt(100);

        if (this.columbid.supportsPairBond() && this.columbid.pairPartner().isPresent()
                && this.columbid.courtshipCooldown <= 0 && roll < 8) {
            this.columbid.courtshipCooldown = 900 + this.columbid.getRandom().nextInt(800);
            this.columbid.setBehaviorStateFor(ColumbidBehaviorState.COURTING, Math.min(this.idleTicks, 90));
            this.columbid.spawnCourtshipParticles(3);
        } else if (roll < 20) {
            this.columbid.getNavigation().stop();
            this.columbid.setBehaviorStateFor(ColumbidBehaviorState.PREENING, Math.min(this.idleTicks, 80));
        } else if (roll < 38) {
            this.columbid.getNavigation().stop();
            this.columbid.setBehaviorStateFor(ColumbidBehaviorState.CURIOUS, Math.min(this.idleTicks, 64));
        } else {
            this.strollTarget = this.columbid.findGroundStrollTarget(5, 2);
            if (this.strollTarget != null) {
                this.columbid.setBehaviorState(this.columbid.birdBrain().wantsForage()
                        ? ColumbidBehaviorState.FORAGING : ColumbidBehaviorState.WALKING);
                this.columbid.getNavigation().moveTo(
                        this.strollTarget.x, this.strollTarget.y, this.strollTarget.z, 0.7
                );
            } else {
                this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
            }
        }
    }

    @Override
    public void tick() {
        --this.idleTicks;

        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.COURTING) {
            Optional<AbstractColumbidEntity> partner = this.columbid.pairPartner();
            partner.ifPresent(p -> this.columbid.getLookControl().setLookAt(p, 25.0F, 25.0F));
        } else if (this.strollTarget == null && this.columbid.getRandom().nextInt(28) == 0) {
            this.columbid.getLookControl().setLookAt(
                    this.columbid.getX() + this.columbid.randomSigned(3.0),
                    this.columbid.getEyeY(),
                    this.columbid.getZ() + this.columbid.randomSigned(3.0),
                    16.0F, 16.0F
            );
        }
    }

    @Override
    public void stop() {
        this.strollTarget = null;
        if (!this.columbid.isControlledFlightActive()
                && this.columbid.getBehaviorState() != ColumbidBehaviorState.EATING
                && this.columbid.getBehaviorState() != ColumbidBehaviorState.ROOSTING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }
}