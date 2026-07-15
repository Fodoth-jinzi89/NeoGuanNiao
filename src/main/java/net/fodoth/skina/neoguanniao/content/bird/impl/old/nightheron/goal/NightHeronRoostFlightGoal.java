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
 * 夜鹭栖息飞行目标
 * 控制夜鹭飞向栖息位置
 */
public class NightHeronRoostFlightGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private BlockPos roostTarget;
    private Vec3 flightDirection;
    private int remainingTicks;

    public NightHeronRoostFlightGoal(NightHeronEntity nightHeron) {
        this.flightDirection = Vec3.ZERO;
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.nightHeron.shouldRoost() && this.nightHeron.onGround()
                && this.nightHeron.getTarget() == null && !this.nightHeron.hasExternalFright()
                && this.nightHeron.getRandom().nextInt(this.roostFlightChance()) == 0) {

            this.roostTarget = NightHeronLandingSelector.findRoostLanding(this.nightHeron, 8, 44);
            return this.roostTarget != null
                    && NightHeronLandingSelector.isRoostingSpot(this.nightHeron.level(), this.roostTarget)
                    && this.roostTarget.getY() >= this.nightHeron.blockPosition().getY() + 1;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.nightHeron.isControlledFlightActive()
                && (this.remainingTicks > 0 || !this.nightHeron.onGround())
                && this.roostTarget != null && !this.nightHeron.hasExternalFright();
    }

    @Override
    public void start() {
        this.remainingTicks = 100 + this.nightHeron.getRandom().nextInt(130);
        this.flightDirection = NightHeronLandingSelector.directionTo(this.roostTarget, this.nightHeron);
        NightHeronFlightController.takeOff(this.nightHeron, this.flightDirection, 0.46, 0.82);
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        this.roostTarget = null;
        this.flightDirection = Vec3.ZERO;
        this.nightHeron.getNavigation().stop();
        this.nightHeron.settleInterruptedFlight(NightHeronBehaviorState.ROOSTING);
    }

    @Override
    public void tick() {
        --this.remainingTicks;
        this.nightHeron.getNavigation().stop();

        if (NightHeronFlightController.shouldBeginLandingApproach(this.nightHeron, this.roostTarget, this.remainingTicks, 14.0)) {
            if (NightHeronFlightController.tickLandingApproach(this.nightHeron, this.roostTarget)) {
                this.remainingTicks = 0;
                this.nightHeron.setBehaviorState(NightHeronBehaviorState.ROOSTING);
            }
        } else {
            this.flightDirection = NightHeronLandingSelector.directionTo(this.roostTarget, this.nightHeron);
            NightHeronFlightController.tickLocalFlight(this.nightHeron, this.flightDirection);
        }
    }

    /**
     * 计算栖息飞行概率
     */
    private int roostFlightChance() {
        return NightHeronLandingSelector.hasRoostCoverNear(this.nightHeron.level(), this.nightHeron.blockPosition(), 5)
                ? 320 : 70;
    }
}