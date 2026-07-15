package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class BudgerigarFlockGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private Vec3 flockTarget;
    private int repathTicks;

    public BudgerigarFlockGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.budgerigar.getRoutineController().isActiveTime() || this.budgerigar.getEatingController().isEating()
                || this.budgerigar.isDancing() || this.budgerigar.getRoutineController().isSleepingOrRoosting()) {
            return false;
        }

        if (this.budgerigar.getRandom().nextInt(50) != 0) {
            return false;
        }

        List<BudgerigarEntity> flock = this.budgerigar.level().getEntitiesOfClass(
                BudgerigarEntity.class,
                this.budgerigar.getBoundingBox().inflate(12.0),
                e -> e != this.budgerigar && !e.isPassenger()
        );

        if (flock.size() < 2) {
            return false;
        }

        // 计算 flock 中心
        Vec3 center = Vec3.ZERO;
        for (BudgerigarEntity member : flock) {
            center = center.add(member.position());
        }
        center = center.scale(1.0 / flock.size());

        // 选择一个靠近中心的目标
        double distance = this.budgerigar.distanceToSqr(center);
        if (distance < 9.0) {
            return false;
        }

        this.flockTarget = center.add(
                (this.budgerigar.getRandom().nextDouble() - 0.5) * 3.0,
                0,
                (this.budgerigar.getRandom().nextDouble() - 0.5) * 3.0
        );
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.flockTarget != null && this.budgerigar.distanceToSqr(this.flockTarget) > 4.0
                && !this.budgerigar.getEatingController().isEating() && !this.budgerigar.isDancing()
                && !this.budgerigar.getRoutineController().isSleepingOrRoosting();
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        if (this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.IDLE
                || this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
        }
    }

    @Override
    public void tick() {
        if (this.flockTarget == null) {
            return;
        }

        if (--this.repathTicks <= 0) {
            this.repathTicks = 10;
            this.budgerigar.getNavigation().moveTo(this.flockTarget.x, this.flockTarget.y, this.flockTarget.z, 0.7);
        }
    }

    @Override
    public void stop() {
        this.flockTarget = null;
        this.repathTicks = 0;
    }
}