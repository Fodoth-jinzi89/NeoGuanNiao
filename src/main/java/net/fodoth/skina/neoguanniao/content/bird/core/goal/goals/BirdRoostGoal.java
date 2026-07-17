package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BirdRoostGoal extends AbstractBirdGoal {

    private BlockPos roostPos;

    public BirdRoostGoal(AbstractBirdEntity<?> bird) {
        super(bird, 60);
    }

    @Override
    protected boolean usePredicates() {
        if (!shouldRoost()) {
            return false;
        }

        if (bird().getRoutineController().isSleeping() && !isGoodRoostPosition(true, bird().blockPosition())) {
            return false;
        }

        if (bird().getRandom().nextInt(goalDatum().roostReFindChance()) != 0) {
            return false;
        }

        this.roostPos = findRoostPosition();
        return this.roostPos != null;
    }

    @Override
    protected boolean continuePredicates() {
        if (!shouldRoost()) {
            return false;
        }
        if (bird().getRoutineController().isSleeping() && isGoodRoostPosition(false, bird().blockPosition())) {
            return false;
        }
        return this.roostPos != null;
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return bird().getRoutineController().isRoosting() || super.defaultContinuePredicates();
    }

    @Override
    protected void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.ROOSTING);
        bird().getNavigation().stop();
    }

    @Override
    protected void onTick() {
        if (this.roostPos == null) {
            return;
        }

        double distance = bird().distanceToSqr(Vec3.atCenterOf(this.roostPos));

        if (distance < goalDatum().roostGoalRange()) {
            // 到达栖息位置
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            bird().setPos(this.roostPos.getX() + 0.5, this.roostPos.getY(), this.roostPos.getZ() + 0.5);
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
    protected void onReset() {
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
    protected void onStop() {
        this.roostPos = null;
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING) {
            if (bird().getRoutineController().isRoosting()) {
                bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            } else {
                bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            }

        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shouldRoost() {
        if (bird().getRoutineController().isRoostTime()) {
            return true;
        }
        return defaultAdditionalPredicates();
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
            }
        } else return false;

        if (state.isAir()) {
            return !bird().level().getBlockState(pos.below()).isAir();
        }

        return false;
    }

}
