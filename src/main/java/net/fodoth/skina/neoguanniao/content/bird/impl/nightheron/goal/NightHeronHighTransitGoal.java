package net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronFlightController;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronLandingSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 夜鹭高空转场目标
 * 控制夜鹭在较远距离间的高空飞行转场
 */
public class NightHeronHighTransitGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private BlockPos landingTarget;
    private Vec3 flightDirection;
    private int remainingTicks;

    public NightHeronHighTransitGoal(NightHeronEntity nightHeron) {
        this.flightDirection = Vec3.ZERO;
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        int chance = this.nightHeron.level().isRaining() ? 650 : 900;
        return this.nightHeron.isActiveTime() && this.nightHeron.onGround()
                && this.nightHeron.getTarget() == null && !this.nightHeron.hasExternalFright()
                && this.nightHeron.getRandom().nextInt(chance) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.nightHeron.isControlledFlightActive()
                && (this.remainingTicks > 0 || !this.nightHeron.onGround())
                && !this.nightHeron.hasExternalFright();
    }

    @Override
    public void start() {
        this.remainingTicks = 120 + this.nightHeron.getRandom().nextInt(101);
        this.landingTarget = NightHeronLandingSelector.findTransitLanding(this.nightHeron, 28, 72);
        this.flightDirection = this.landingTarget != null
                ? NightHeronLandingSelector.directionTo(this.landingTarget, this.nightHeron)
                : this.randomDirection();
        NightHeronFlightController.takeOff(this.nightHeron, this.flightDirection, 0.36, 0.72);
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        this.landingTarget = null;
        this.flightDirection = Vec3.ZERO;
        this.nightHeron.getNavigation().stop();
        this.nightHeron.settleInterruptedFlight(NightHeronBehaviorState.IDLE);
    }

    @Override
    public void tick() {
        --this.remainingTicks;
        this.nightHeron.getNavigation().stop();

        if (this.remainingTicks <= 0 && this.landingTarget == null) {
            this.landingTarget = NightHeronLandingSelector.findTransitLanding(this.nightHeron, 10, 36);
            if (this.landingTarget == null) {
                NightHeronFlightController.tickOpenLanding(this.nightHeron, this.flightDirection);
                return;
            }
        }

        if (NightHeronFlightController.shouldBeginLandingApproach(
                this.nightHeron, this.landingTarget, this.remainingTicks, 26.0)) {
            if (NightHeronFlightController.tickLandingApproach(this.nightHeron, this.landingTarget)) {
                this.remainingTicks = 0;
            }
        } else {
            if (this.landingTarget != null) {
                this.flightDirection = NightHeronLandingSelector.directionTo(this.landingTarget, this.nightHeron);
            } else if (this.flightDirection.lengthSqr() <= 1.0E-4 || this.nightHeron.isInWater() || this.remainingTicks % 40 == 0) {
                this.flightDirection = this.randomDirection();
            }

            NightHeronFlightController.tickHighTransitFlight(this.nightHeron, this.flightDirection);
        }
    }

    /**
     * 生成随机水平方向
     */
    private Vec3 randomDirection() {
        float angle = this.nightHeron.getRandom().nextFloat() * (float) (2.0 * Math.PI);
        return new Vec3(Mth.cos(angle), 0.0, Mth.sin(angle));
    }
}