package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BirdIdleGoal extends AbstractBirdGoal {

    private BlockPos targetPos;
    public BirdIdleGoal(AbstractBirdEntity<?> bird) {
        super(bird, 60);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    protected boolean usePredicates() {
        if (!defaultAdditionalPredicates() || bird().getFlyingController().isFlightInProgress()) {
            return false;
        }

        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.IDLE
                && bird().getRandom().nextInt(goalDatum().idleRetargetChance()) != 0) {
            return false;
        }

        return this.findTargetPosition();
    }

    @Override
    protected boolean continuePredicates() {
        if (!defaultAdditionalPredicates() || bird().getFlyingController().isFlightInProgress()) {
            return false;
        }
        return this.targetPos != null
                && bird().distanceToSqr(Vec3.atCenterOf(this.targetPos)) > goalDatum().idleStopDistance();
    }

    @Override
    protected void onStart() {
        if (bird().getBehaviorStateController().getBehaviorState() != BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
        }
    }

    @Override
    protected void onTick() {
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
    protected void onReset() {
        if (this.targetPos == null) {
            return;
        }
        bird().getNavigation().moveTo(
                this.targetPos.getX() + 0.5,
                this.targetPos.getY(),
                this.targetPos.getZ() + 0.5,
                goalDatum().idleMoveSpeed()
        );
    }

    @Override
    protected void onStop() {
        this.targetPos = null;
        bird().getNavigation().stop();
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }

    private boolean findTargetPosition() {
        BlockPos origin = bird().blockPosition();

        for (int attempt = 0; attempt < goalDatum().idleFindTargetMaxAttempts(); attempt++) {
            int x = origin.getX() + bird().getRandom().nextInt(goalDatum().idleFindTargetXRange()) - (goalDatum().idleFindTargetXRange() / 2);
            int z = origin.getZ() + bird().getRandom().nextInt(goalDatum().idleFindTargetZRange()) - (goalDatum().idleFindTargetZRange() / 2);
            int y = origin.getY() + bird().getRandom().nextInt(goalDatum().idleFindTargetYRange()) - (goalDatum().idleFindTargetYRange() / 2);

            BlockPos pos = new BlockPos(x, y, z);

            // 检查位置是否可到达
            if (bird().getNavigation().isStableDestination(pos)) {
                // 检查是否安全
                if (BirdFlightTargeting.isSafeDryLanding(bird(), pos)) {
                    this.targetPos = pos;
                    return true;
                }
            }
        }

        return false;
    }
}
