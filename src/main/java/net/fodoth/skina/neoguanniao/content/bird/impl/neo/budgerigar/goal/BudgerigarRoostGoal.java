package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BudgerigarRoostGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private BlockPos roostPos;
    private int repathTicks;

    public BudgerigarRoostGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.budgerigar.getRoutineController().isRoostTime() || this.budgerigar.getEatingController().isEating() || this.budgerigar.isDancing()) {
            return false;
        }

        if (this.budgerigar.getRandom().nextInt(60) != 0) {
            return false;
        }

        this.roostPos = this.findRoostPosition();
        return this.roostPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.roostPos != null && this.budgerigar.getRoutineController().isRoostTime()
                && !this.budgerigar.getEatingController().isEating() && !this.budgerigar.isDancing();
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.ROOSTING);
        this.budgerigar.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.roostPos == null) {
            return;
        }

        double distance = this.budgerigar.distanceToSqr(Vec3.atCenterOf(this.roostPos));

        if (distance < 4.0) {
            // 到达栖息位置
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            this.budgerigar.setPos(this.roostPos.getX() + 0.5, this.roostPos.getY(), this.roostPos.getZ() + 0.5);
            return;
        }

        if (--this.repathTicks <= 0) {
            this.repathTicks = 10;
            this.budgerigar.getNavigation().moveTo(
                    this.roostPos.getX() + 0.5,
                    this.roostPos.getY(),
                    this.roostPos.getZ() + 0.5,
                    0.6
            );
        }

        this.budgerigar.getLookControl().setLookAt(
                this.roostPos.getX() + 0.5,
                this.roostPos.getY(),
                this.roostPos.getZ() + 0.5,
                20.0F, 20.0F
        );
    }

    @Override
    public void stop() {
        this.roostPos = null;
        this.repathTicks = 0;
        if (this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING
                || this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SLEEPING) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }

    private BlockPos findRoostPosition() {
        BlockPos origin = this.budgerigar.blockPosition();

        for (int attempt = 0; attempt < 20; attempt++) {
            int x = origin.getX() + this.budgerigar.getRandom().nextInt(15) - 7;
            int z = origin.getZ() + this.budgerigar.getRandom().nextInt(15) - 7;
            int y = origin.getY() + this.budgerigar.getRandom().nextInt(8) + 2;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.budgerigar.level().getBlockState(pos);

            if (state.getBlock() instanceof LeavesBlock) {
                BlockPos below = pos.below();
                BlockState belowState = this.budgerigar.level().getBlockState(below);
                if (belowState.isAir() || belowState.is(Blocks.AIR)) {
                    return pos;
                }
            }
        }

        // 如果找不到树叶，尝试其他高处
        for (int attempt = 0; attempt < 15; attempt++) {
            int x = origin.getX() + this.budgerigar.getRandom().nextInt(10) - 5;
            int z = origin.getZ() + this.budgerigar.getRandom().nextInt(10) - 5;
            int y = origin.getY() + this.budgerigar.getRandom().nextInt(6) + 3;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.budgerigar.level().getBlockState(pos);

            if (state.isAir() || state.is(Blocks.AIR)) {
                BlockPos below = pos.below();
                BlockState belowState = this.budgerigar.level().getBlockState(below);
                if (!belowState.isAir() && !belowState.is(Blocks.AIR)) {
                    return pos;
                }
            }
        }

        return null;
    }
}