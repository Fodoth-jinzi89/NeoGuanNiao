package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronFlightController;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronLandingSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 夜鹭环境飞行目标
 * 控制夜鹭在环境中的自主飞行行为，包括本地飞行和翱翔
 */
public class NightHeronAmbientFlightGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private BlockPos landingTarget;
    private Vec3 flightDirection;
    private FlightKind flightKind;
    private int remainingTicks;

    public NightHeronAmbientFlightGoal(NightHeronEntity nightHeron) {
        this.flightDirection = Vec3.ZERO;
        this.flightKind = FlightKind.LOCAL;
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.nightHeron.onGround() && this.nightHeron.getTarget() == null && !this.nightHeron.hasExternalFright()) {
            int chance = this.nightHeron.isActiveTime() ? 120 : 300;
            if (this.nightHeron.getRandom().nextInt(chance) != 0) {
                return false;
            }

            this.flightKind = this.chooseFlightKind();
            int minRadius = this.flightKind == FlightKind.SOARING ? 30 : 10;
            int maxRadius = this.flightKind == FlightKind.SOARING ? 82 : 44;
            this.landingTarget = NightHeronLandingSelector.findTransitLanding(this.nightHeron, minRadius, maxRadius);
            return this.landingTarget != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.nightHeron.isControlledFlightActive()
                && (this.remainingTicks > 0 || !this.nightHeron.onGround())
                && this.landingTarget != null
                && !this.nightHeron.hasExternalFright();
    }

    @Override
    public void start() {
        this.remainingTicks = this.flightKind == FlightKind.SOARING
                ? this.randomBetween(190, 360)
                : this.randomBetween(100, 220);
        this.flightDirection = NightHeronLandingSelector.directionTo(this.landingTarget, this.nightHeron);
        NightHeronFlightController.takeOff(this.nightHeron, this.flightDirection, 0.56, 0.82);
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

        double approachDistance = this.flightKind == FlightKind.SOARING ? 26.0 : 14.0;
        if (NightHeronFlightController.shouldBeginLandingApproach(this.nightHeron, this.landingTarget, this.remainingTicks, approachDistance)) {
            if (NightHeronFlightController.tickLandingApproach(this.nightHeron, this.landingTarget)) {
                this.remainingTicks = 0;
            }
        } else {
            this.flightDirection = NightHeronLandingSelector.directionTo(this.landingTarget, this.nightHeron);
            if (this.flightKind == FlightKind.SOARING) {
                NightHeronFlightController.tickSoaringFlight(this.nightHeron, this.flightDirection);
            } else {
                NightHeronFlightController.tickLocalFlight(this.nightHeron, this.flightDirection);
            }
        }
    }

    /**
     * 选择飞行类型
     */
    private FlightKind chooseFlightKind() {
        return this.nightHeron.isActiveTime() && this.nightHeron.getRandom().nextInt(4) == 0
                ? FlightKind.SOARING
                : FlightKind.LOCAL;
    }

    /**
     * 生成随机值
     */
    private int randomBetween(int min, int max) {
        return min + this.nightHeron.getRandom().nextInt(max - min + 1);
    }

    /**
     * 飞行类型枚举
     */
    private enum FlightKind {
        /** 本地短途飞行 */
        LOCAL,
        /** 高空翱翔 */
        SOARING
    }
}