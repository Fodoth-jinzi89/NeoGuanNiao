package net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BudgerigarSentinelGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private int sentinelTicks;

    public BudgerigarSentinelGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.budgerigar.isActiveTime()
                && !this.budgerigar.isEating()
                && !this.budgerigar.isDancing()
                && !this.budgerigar.isSleepingOrRoosting()
                && this.budgerigar.getRandom().nextInt(120) == 0;
    }

    @Override
    public void start() {
        this.sentinelTicks = 60 + this.budgerigar.getRandom().nextInt(80);
        this.budgerigar.setBehaviorState(BudgerigarBehaviorState.SENTINEL);
        this.budgerigar.getNavigation().stop();

        // 看向随机方向
        Vec3 lookAt = new Vec3(
                this.budgerigar.getX() + this.budgerigar.getRandom().nextGaussian() * 3.0,
                this.budgerigar.getY(),
                this.budgerigar.getZ() + this.budgerigar.getRandom().nextGaussian() * 3.0
        );
        this.budgerigar.getLookControl().setLookAt(lookAt.x, lookAt.y, lookAt.z, 30.0F, 30.0F);
    }

    @Override
    public boolean canContinueToUse() {
        return this.sentinelTicks > 0
                && !this.budgerigar.isEating()
                && !this.budgerigar.isDancing()
                && !this.budgerigar.isSleepingOrRoosting();
    }

    @Override
    public void tick() {
        this.sentinelTicks--;
        if (this.sentinelTicks % 20 == 0) {
            // 定期环顾四周
            float yaw = this.budgerigar.getYRot() + 45.0F * (this.budgerigar.getRandom().nextBoolean() ? 1 : -1);
            this.budgerigar.setYRot(yaw);
            this.budgerigar.yBodyRot = yaw;
        }
    }

    @Override
    public void stop() {
        this.sentinelTicks = 0;
        if (this.budgerigar.getBehaviorState() == BudgerigarBehaviorState.SENTINEL) {
            this.budgerigar.setBehaviorState(BudgerigarBehaviorState.IDLE);
        }
    }
}