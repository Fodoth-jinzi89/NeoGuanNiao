package net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BudgerigarIdleGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private BlockPos targetPos;
    private int repathTicks;

    public BudgerigarIdleGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.budgerigar.isActiveTime() || this.budgerigar.isEating()
                || this.budgerigar.isDancing() || this.budgerigar.isSleepingOrRoosting()
                || this.budgerigar.isFlightInProgress()) {
            return false;
        }

        if (this.budgerigar.getRandom().nextInt(60) != 0) {
            return false;
        }

        // 如果已经空闲，更可能开始闲逛
        if (this.budgerigar.getBehaviorState() == BudgerigarBehaviorState.IDLE
                && this.budgerigar.getRandom().nextInt(3) != 0) {
            return false;
        }

        return this.findTargetPosition();
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
                && !this.budgerigar.isEating() && !this.budgerigar.isDancing()
                && !this.budgerigar.isSleepingOrRoosting()
                && !this.budgerigar.isFlightInProgress()
                && this.budgerigar.distanceToSqr(Vec3.atCenterOf(this.targetPos)) > 1.0;
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        if (this.budgerigar.getBehaviorState() != BudgerigarBehaviorState.WALKING) {
            this.budgerigar.setBehaviorState(BudgerigarBehaviorState.WALKING);
        }
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        if (--this.repathTicks <= 0) {
            this.repathTicks = 10;
            this.budgerigar.getNavigation().moveTo(
                    this.targetPos.getX() + 0.5,
                    this.targetPos.getY(),
                    this.targetPos.getZ() + 0.5,
                    0.5
            );
        }

        // 偶尔看看周围
        if (this.budgerigar.getRandom().nextInt(40) == 0) {
            float yaw = this.budgerigar.getYRot() + (this.budgerigar.getRandom().nextFloat() - 0.5F) * 60.0F;
            this.budgerigar.setYRot(yaw);
            this.budgerigar.yBodyRot = yaw;
        }
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.repathTicks = 0;
        this.budgerigar.getNavigation().stop();
        if (this.budgerigar.getBehaviorState() == BudgerigarBehaviorState.WALKING) {
            this.budgerigar.setBehaviorState(BudgerigarBehaviorState.IDLE);
        }
    }

    private boolean findTargetPosition() {
        BlockPos origin = this.budgerigar.blockPosition();

        for (int attempt = 0; attempt < 15; attempt++) {
            int x = origin.getX() + this.budgerigar.getRandom().nextInt(11) - 5;
            int z = origin.getZ() + this.budgerigar.getRandom().nextInt(11) - 5;
            int y = origin.getY() + this.budgerigar.getRandom().nextInt(5) - 2;

            BlockPos pos = new BlockPos(x, y, z);

            // 检查位置是否可到达
            if (this.budgerigar.getNavigation().isStableDestination(pos)) {
                // 检查是否安全
                if (this.budgerigar.level().getBlockState(pos).isAir()
                        && !this.budgerigar.level().getBlockState(pos.below()).isAir()) {
                    this.targetPos = pos;
                    return true;
                }
            }
        }

        return false;
    }
}