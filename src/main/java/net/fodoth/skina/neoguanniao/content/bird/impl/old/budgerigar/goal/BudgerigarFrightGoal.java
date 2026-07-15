package net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BudgerigarFrightGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private int fleeTicks;

    public BudgerigarFrightGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.budgerigar.shouldFlee() && !this.budgerigar.isFlightInProgress();
    }

    @Override
    public boolean canContinueToUse() {
        return this.budgerigar.shouldFlee() && this.fleeTicks > 0;
    }

    @Override
    public void start() {
        this.fleeTicks = 60 + this.budgerigar.getRandom().nextInt(40);
        Vec3 source = this.budgerigar.frightSource();
        if (source != null) {
            Vec3 away = this.budgerigar.position().subtract(source);
            if (away.lengthSqr() > 0.01) {
                Vec3 target = this.budgerigar.position().add(away.normalize().scale(8.0));
                this.budgerigar.startShortFlight(target, true);
            }
        }
    }

    @Override
    public void tick() {
        this.fleeTicks--;
        if (this.fleeTicks <= 0) {
            this.budgerigar.setBehaviorStateFor(BudgerigarBehaviorState.ALERT, 20);
        }
    }

    @Override
    public void stop() {
        this.fleeTicks = 0;
    }
}