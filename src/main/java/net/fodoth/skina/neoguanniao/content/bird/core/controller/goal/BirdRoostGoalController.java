package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


// TODO 兼容原版Sleeping系统，鸟可以睡在书架啦床啦之类的方块，可以占用床，搞个标签
public class BirdRoostGoalController<T extends AbstractBirdEntity<T>> extends AbstractGoalController<T> {
    private BlockPos roostPos;

    @Override
    public int chance() {
        return goalDatum().randomLookAroundChance();
    }

    @Override
    public boolean canUse() {
        if (!shouldRoost()) {
            return false;
        }

        // 幼鸟不会飞，也不会选择更好的栖息地
        if (bird().getRoutineController().isSleeping() && ( bird().isBaby() || isGoodRoostPosition(true, bird().blockPosition()))) {
            return false;
        }

        return bird().getRandom().nextInt(goalDatum().roostReFindChance()) == 0;
    }

    @Override
    public boolean onUse() {
        this.roostPos = findRoostPosition();
        return this.roostPos != null;
    }

    @Override
    public boolean canContinue() {
        if (!shouldRoost()) {
            return false;
        }
        // 幼鸟不会飞，只能睡在地上
        if (bird().isBaby()) {
            return false;
        }
        if (bird().getRoutineController().isSleeping() && isGoodRoostPosition(false, bird().blockPosition())) {
            return false;
        }
        return this.roostPos != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shouldRoost() {
        return bird().getRoutineController().isRoostTime() && !bird().getEatingController().isEating()
                && !bird().isDancing()
                && !bird().getBehaviorStateController().getBehaviorState().isEscape();
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.ROOSTING);
        bird().getNavigation().stop();
    }

    @Override
    public void onTick() {
        if (this.roostPos == null) {
            return;
        }

        double distance = bird().distanceToSqr(Vec3.atCenterOf(this.roostPos));

        if (distance < goalDatum().roostGoalRange()) {
            // 到达栖息位置
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            bird().setPos(this.roostPos.getX() + 0.5, this.roostPos.getY() - 0.5, this.roostPos.getZ() + 0.5);
            bird().setNoGravity(true);
            return;
        }


        bird().getLookControl().setLookAt(
                this.roostPos.getX() + 0.5,
                this.roostPos.getY(),
                this.roostPos.getZ() + 0.5,
                goalDatum().roostLookYaw(), goalDatum().roostLookPitch()
        );
    }

    @Override
    public void onReset() {
        if (this.roostPos == null) {
            return;
        }
        bird().getNavigation().moveTo(
                this.roostPos.getX() + 0.5,
                this.roostPos.getY(),
                this.roostPos.getZ() + 0.5,
                goalDatum().roostMoveSpeed()
        );
    }

    @Override
    public void onStop() {
        this.roostPos = null;
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING) {
            if (bird().getRoutineController().isRoosting()) {
                bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            } else {
                bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            }

        }
    }


    private BlockPos findRoostPosition() {
        BlockPos origin = bird().blockPosition();

        for (int attempt = 0; attempt < goalDatum().roostFindTargetMaxAttempts(); attempt++) {
            int x = origin.getX() + bird().getRandom().nextInt(goalDatum().roostFindTargetXRange()) - goalDatum().roostFindTargetXRange() / 2;
            int z = origin.getZ() + (goalDatum().roostFindTargetZRange()) - goalDatum().roostFindTargetZRange() / 2;
            int y = origin.getY() + bird().getRandom().nextInt(goalDatum().roostFindTargetYRange()) + goalDatum().roostFindTargetYOffset();

            BlockPos pos = new BlockPos(x, y, z);

            if (isGoodRoostPosition(true, pos)) {
                return pos;
            }
        }

        // 如果找不到树叶，尝试其他高处
        for (int attempt = 0; attempt < goalDatum().roostFallbackTargetMaxAttempts(); attempt++) {
            int x = origin.getX() + bird().getRandom().nextInt(goalDatum().roostFallbackTargetXRange()) - goalDatum().roostFallbackTargetXRange() / 2;
            int z = origin.getZ() + (goalDatum().roostFallbackTargetZRange()) - goalDatum().roostFallbackTargetZRange() / 2;
            int y = origin.getY() + bird().getRandom().nextInt(goalDatum().roostFallbackTargetYRange()) + goalDatum().roostFallbackTargetYOffset();

            BlockPos pos = new BlockPos(x, y, z);

            if (isGoodRoostPosition(false, pos)) {
                return pos;
            }
        }

        return null;
    }

    private boolean isGoodRoostPosition(boolean strict, BlockPos pos) {
        BlockState state = bird().level().getBlockState(pos);

        // 树叶
        if (strict) {
            if (state.getBlock() instanceof LeavesBlock) {
                var block = bird().level().getBlockState(pos.below());
                return block.isAir() || block.getBlock() instanceof LeavesBlock;
            } else return false;
        }

        if (state.isAir()) {
            return !bird().level().getBlockState(pos.below()).isAir();
        }

        return false;
    }
}
