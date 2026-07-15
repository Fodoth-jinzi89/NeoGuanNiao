package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.ColumbidBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ColumbidAmbientFlightGoal extends Goal {
    private final AbstractColumbidEntity columbid;

    public ColumbidAmbientFlightGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.canStartGroundSocialGoal() || !this.columbid.getNavigation().isDone()
                || this.columbid.flightCooldown <= 0
                || this.columbid.getBehaviorState() == ColumbidBehaviorState.FORAGING
                || this.columbid.getBehaviorState() == ColumbidBehaviorState.EATING) {
            return false;
        }
        return this.columbid.getRandom().nextInt(Math.max(80, this.columbid.ambientFlightChance())) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        Vec3 direction = this.columbid.randomHorizontalDirection();
        if (this.columbid.getRandom().nextInt(4) == 0) {
            direction = this.columbid.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() <= 1.0E-4) {
            direction = this.columbid.randomHorizontalDirection();
        }

        Vec3 target = this.columbid.findFlightLandingTarget(direction, 52, 96, true);
        if (target == null) {
            target = this.columbid.findFlightLandingTarget(direction, 28, 54, true);
        }

        if (target != null) {
            int duration = 520 + this.columbid.getRandom().nextInt(300);
            this.columbid.startAutonomousCruiseFlight(target, duration);
        }
    }
}