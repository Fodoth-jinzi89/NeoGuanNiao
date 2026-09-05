package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BirdIdleGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
    private BlockPos targetPos;

    @Override
    public int chance() {
        return goalDatum().idleChance();
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !bird().getFlyingController().isFlightInProgress() && !bird().isBaby();
    }

    @Override
    public boolean onUse() {
        if (bird().getRandom().nextInt(goalDatum().idleRetargetChance()) != 0) {
            return false;
        }

        var state = bird().getBehaviorStateController().getBehaviorState();
        if (state != BirdBehaviorState.IDLE && state != BirdBehaviorState.SENTINEL) {
            return false;
        }

        return this.findTargetPosition();
    }


    @Override
    public boolean onContinue() {
        return this.targetPos != null
                && bird().distanceToSqr(Vec3.atCenterOf(this.targetPos)) > goalDatum().idleStopDistance();
    }

    @Override
    public void onStart() {
        if (bird().getBehaviorStateController().getBehaviorState() != BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
        }
    }

    @Override
    public void onTick() {
        if (this.targetPos == null) {
            return;
        }

        // 偶尔看看周围
        if (bird().getRandom().nextInt(goalDatum().idleLookAroundChance()) == 0) {
            float yaw = bird().getYRot() + (bird().getRandom().nextFloat() - 0.5F) * goalDatum().idleLookAroundChance();
            bird().setYRot(yaw);
            bird().yBodyRot = yaw;
        }
    }

    @Override
    public void onReset() {
        if (this.targetPos == null) {
            return;
        }
        if (bird().getY() >= targetPos.getY()) {
            bird().getNavigation().setCanFloat(false);
        }
        bird().getNavigation().moveTo(
                this.targetPos.getX() + 0.5,
                this.targetPos.getY(),
                this.targetPos.getZ() + 0.5,
                goalDatum().idleMoveSpeed()
        );
    }

    @Override
    public void onStop() {
        this.targetPos = null;
        bird().getNavigation().stop();
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }



    private boolean findTargetPosition() {
        BlockPos origin = bird().blockPosition();

        int xRange = goalDatum().idleFindTargetXRange();
        int yRange = goalDatum().idleFindTargetYRange();
        int zRange = goalDatum().idleFindTargetZRange();

        int minY = origin.getY() - yRange / 2;
        int maxY = origin.getY() + yRange / 2;

        for (int attempt = 0; attempt < goalDatum().idleFindTargetMaxAttempts(); attempt++) {

            int x = origin.getX() + randomOffset(bird().getRandom(), xRange, goalDatum().idleFindTargetMinRange());
            int z = origin.getZ() + randomOffset(bird().getRandom(), zRange, goalDatum().idleFindTargetMinRange());


            // 从上向下扫描这一列
            for (int y = maxY; y >= minY; y--) {

                BlockPos groundPos = new BlockPos(x, y, z);
                BlockState state = bird().level().getBlockState(groundPos);

                // 找到第一个具有碰撞箱的方块
                if (state.getCollisionShape(bird().level(), groundPos).isEmpty()) {
                    continue;
                }

                BlockPos targetPos = groundPos.above();

                if (!bird().getFlyingController().isSafeDryLandingOrAir(targetPos)) {
                    continue;
                }

                this.targetPos = targetPos;
                return true;
            }
        }

        return false;
    }

    private int randomOffset(RandomSource random, int range, int minDistance) {
        int half = range / 2;

        if (random.nextBoolean()) {
            return -half + random.nextInt(half - minDistance + 1);
        } else {
            return minDistance + random.nextInt(half - minDistance + 1);
        }
    }
}
