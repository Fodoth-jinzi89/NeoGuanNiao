package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ColumbidGroundForagingGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private Vec3 strollTarget;
    private int remainingTicks;
    private int repathTicks;
    private int peckCooldown;
    private int lookCooldown;

    public ColumbidGroundForagingGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.canStartGroundSocialGoal() || !this.columbid.getNavigation().isDone()) {
            return false;
        }
        int chance = this.columbid.birdBrain().wantsForage() ? 5
                : (this.columbid.prefersHumanSettlements() ? 12 : 16);
        if (this.columbid.sensesIncomingBadWeather()) {
            chance += 8;
        }
        return this.columbid.getRandom().nextInt(chance) == 0;
    }

    @Override    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.columbid.canStartGroundSocialGoal()
                && !this.columbid.isRoostTime();
    }

    @Override
    public void start() {
        this.remainingTicks = 120 + this.columbid.getRandom().nextInt(141);
        this.repathTicks = 0;
        this.peckCooldown = 8 + this.columbid.getRandom().nextInt(18);
        this.lookCooldown = 20 + this.columbid.getRandom().nextInt(35);
        this.columbid.setBehaviorState(ColumbidBehaviorState.FORAGING);
    }

    @Override
    public void tick() {
        --this.remainingTicks;
        if (this.repathTicks > 0) --this.repathTicks;
        if (this.peckCooldown > 0) --this.peckCooldown;
        if (this.lookCooldown > 0) --this.lookCooldown;

        if (this.columbid.behaviorStateLockTicks <= 0 || !this.columbid.getNavigation().isDone()) {
            if (this.peckCooldown <= 0 && this.columbid.getNavigation().isDone()) {
                this.peckAtGround();
                this.peckCooldown = 22 + this.columbid.getRandom().nextInt(36);
            } else {
                if (this.lookCooldown <= 0 && this.columbid.getNavigation().isDone()) {
                    this.lookAround();
                    this.lookCooldown = 32 + this.columbid.getRandom().nextInt(46);
                }
                if (this.repathTicks <= 0 && (this.columbid.getNavigation().isDone()
                        || this.strollTarget == null || this.columbid.distanceToSqr(this.strollTarget) < 1.8)) {
                    this.chooseStrollTarget();
                }
                if (this.strollTarget != null && this.columbid.getNavigation().isDone()) {
                    this.columbid.setBehaviorState(ColumbidBehaviorState.FORAGING);
                    this.columbid.getNavigation().moveTo(
                            this.strollTarget.x, this.strollTarget.y, this.strollTarget.z,
                            this.columbid.prefersHumanSettlements() ? 0.78 : 0.7
                    );
                }
            }
        }
    }

    @Override
    public void stop() {
        this.strollTarget = null;
        this.remainingTicks = 0;
        if (!this.columbid.isControlledFlightActive()
                && this.columbid.getBehaviorState() != ColumbidBehaviorState.EATING
                && this.columbid.getBehaviorState() != ColumbidBehaviorState.ROOSTING
                && this.columbid.getBehaviorState() != ColumbidBehaviorState.SLEEPING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }

    private void chooseStrollTarget() {
        this.repathTicks = 24 + this.columbid.getRandom().nextInt(28);
        this.strollTarget = this.columbid.findGroundStrollTarget(
                this.columbid.prefersHumanSettlements() ? 7 : 5, 2
        );
    }

    private void peckAtGround() {
        this.columbid.getNavigation().stop();
        double x = this.columbid.getX() + this.columbid.randomSigned(0.45);
        double z = this.columbid.getZ() + this.columbid.randomSigned(0.45);
        this.columbid.getLookControl().setLookAt(x, this.columbid.getY() + 0.1, z, 18.0F, 18.0F);
        this.columbid.triggerPeckAnimation(18 + this.columbid.getRandom().nextInt(10));
    }

    private void lookAround() {
        double x = this.columbid.getX() + this.columbid.randomSigned(3.5);
        double z = this.columbid.getZ() + this.columbid.randomSigned(3.5);
        this.columbid.getLookControl().setLookAt(x, this.columbid.getEyeY(), z, 16.0F, 16.0F);
        this.columbid.setBehaviorStateFor(ColumbidBehaviorState.CURIOUS, 18 + this.columbid.getRandom().nextInt(18));
    }
}