package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronLandingSelector;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 夜鹭栖息目标
 * 控制夜鹭在地面进行栖息行为
 */
public class NightHeronRoostGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private int remainingTicks;

    public NightHeronRoostGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.nightHeron.shouldRoost() && this.nightHeron.onGround()
                && this.nightHeron.getTarget() == null && this.isNearRoostCover()
                && this.nightHeron.getRandom().nextInt(55) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.nightHeron.shouldRoost()
                && this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.remainingTicks = 110 + this.nightHeron.getRandom().nextInt(190);
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.ROOSTING);
        if (this.nightHeron.getNavigation().isDone()) {
            this.nightHeron.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        if (!this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    @Override
    public void tick() {
        --this.remainingTicks;

        if (this.nightHeron.getBehaviorState() != NightHeronBehaviorState.SOCIAL_SPACING) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.ROOSTING);
        }

        if (this.remainingTicks % 70 == 0 && this.nightHeron.getRandom().nextInt(3) == 0) {
            this.nightHeron.triggerNeckStretch();
        }
    }

    /**
     * 检查是否靠近栖息遮蔽物
     */
    private boolean isNearRoostCover() {
        return NightHeronLandingSelector.isRoostingSpot(this.nightHeron.level(), this.nightHeron.blockPosition())
                || NightHeronLandingSelector.hasRoostCoverNear(this.nightHeron.level(), this.nightHeron.blockPosition(), 5);
    }
}