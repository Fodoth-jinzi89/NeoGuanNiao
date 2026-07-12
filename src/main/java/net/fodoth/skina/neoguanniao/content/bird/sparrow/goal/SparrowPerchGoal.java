package net.fodoth.skina.neoguanniao.content.bird.sparrow.goal;

import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SparrowPerchGoal extends Goal {
    private final SparrowEntity sparrow;
    private BlockPos perchPos;
    private boolean roosting;
    private int remainingTicks;
    private int repositionTicks;

    public SparrowPerchGoal(SparrowEntity sparrow) {
        this.sparrow = sparrow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    @SuppressWarnings("all")
    public boolean canUse() {
        if (this.sparrow.pendingScareTicks <= 0 && !this.sparrow.isControlledFlightActive()
                && !this.sparrow.isInWater() && this.sparrow.getTarget() == null) {
            boolean brainRoost = this.sparrow.brainWantsRoost();
            this.roosting = this.sparrow.shouldSeekNightRoost() || brainRoost;

            if (!this.roosting) {
                int chance = brainRoost ? 80 : (this.sparrow.isTame() ? 280 : 190);
                if (!this.sparrow.onGround() || this.sparrow.perchCooldown > 0
                        || !this.sparrow.getNavigation().isDone()
                        || this.sparrow.getRandom().nextInt(chance) != 0) {
                    return false;
                }
            } else if (this.sparrow.perchCooldown > 0 && this.sparrow.getRandom().nextInt(4) != 0) {
                return false;
            }

            this.perchPos = this.sparrow.findPerchTarget(this.roosting);
            return this.perchPos != null;
        }
        return false;
    }

    @Override
    public void start() {
        this.remainingTicks = this.roosting ? this.sparrow.randomBetween(360, 760) : this.sparrow.randomBetween(90, 190);
        this.repositionTicks = 0;
        this.sparrow.setBehaviorState(this.roosting ? SparrowBehaviorState.ROOSTING : SparrowBehaviorState.PERCHING);
        this.moveToPerch();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.perchPos != null && this.remainingTicks > 0 && this.sparrow.pendingScareTicks <= 0
                && !this.sparrow.isInWater()) {
            if (!this.sparrow.isSafePerchPosition(this.perchPos)) {
                return false;
            }
            return this.roosting ? this.sparrow.shouldSeekNightRoost() || this.sparrow.brainWantsRoost()
                    : !this.sparrow.shouldSeekNightRoost();
        }
        return false;
    }



    @Override
    public void tick() {
        --this.remainingTicks;
        if (this.perchPos == null) return;

        this.sparrow.setBehaviorState(this.roosting ? SparrowBehaviorState.ROOSTING : SparrowBehaviorState.PERCHING);

        if (this.sparrow.isControlledFlightActive()) {
            this.sparrow.getLookControl().setLookAt(
                    this.perchPos.getX() + 0.5, this.perchPos.getY() + 0.2, this.perchPos.getZ() + 0.5,
                    20.0F, 20.0F);
        } else {
            double distanceSqr = this.sparrow.position().distanceToSqr(Vec3.atBottomCenterOf(this.perchPos));
            if (distanceSqr > 1.35) {
                if (--this.repositionTicks <= 0 || this.sparrow.getNavigation().isDone()) {
                    this.moveToPerch();
                }
            } else {
                this.sparrow.getNavigation().stop();
                this.sparrow.getLookControl().setLookAt(
                        this.perchPos.getX() + 0.5 + this.sparrow.randomSigned(0.8),
                        this.perchPos.getY() + 0.4,
                        this.perchPos.getZ() + 0.5 + this.sparrow.randomSigned(0.8),
                        12.0F, 12.0F);

                if (this.remainingTicks % 80 == 0 && this.sparrow.getRandom().nextInt(3) == 0) {
                    this.sparrow.triggerTailFlick();
                } else if (this.remainingTicks % 95 == 0 && this.sparrow.getRandom().nextInt(4) == 0) {
                    this.sparrow.triggerLookAround();
                }

                if (this.remainingTicks % 80 == 0) {
                    this.sparrow.birdBrain().onRest(0.02F);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.perchPos = null;
        this.remainingTicks = 0;
        this.repositionTicks = 0;
        this.sparrow.perchCooldown = this.roosting ? 60 + this.sparrow.getRandom().nextInt(80)
                : 420 + this.sparrow.getRandom().nextInt(360);
        if (this.sparrow.getBehaviorState() == SparrowBehaviorState.PERCHING
                || this.sparrow.getBehaviorState() == SparrowBehaviorState.ROOSTING) {
            this.sparrow.setBehaviorState(SparrowBehaviorState.IDLE);
        }
    }

    private void moveToPerch() {
        if (this.perchPos == null) return;
        this.repositionTicks = 24;
        Vec3 target = Vec3.atBottomCenterOf(this.perchPos);
        double distanceSqr = this.sparrow.position().distanceToSqr(target);
        boolean highTarget = this.perchPos.getY() > this.sparrow.blockPosition().getY() + 2;
        boolean shouldFlyToPerch = this.roosting && this.sparrow.onGround()
                && this.sparrow.flightCooldown <= 0 && (distanceSqr > 144.0 || highTarget);

        if (!shouldFlyToPerch || !this.sparrow.startControlledFlight(
                new Vec3(target.x, target.y + 0.04, target.z),
                this.sparrow.randomBetween(26, 48),
                0.24 + (this.roosting ? 0.05 : 0.02),
                false)) {
            this.sparrow.getNavigation().moveTo(target.x, target.y, target.z, this.roosting ? 0.96 : 0.84);
        }
    }
}