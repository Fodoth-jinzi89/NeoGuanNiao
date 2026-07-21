package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BirdFlockGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
    private Vec3 flockTarget;

    @Override
    public int chance() {
        return goalDatum().flockChance();
    }


    @Override
    public boolean onUse() {
        var goalDatum = bird().getBirdData().goal();
        List<BudgerigarEntity> flock = bird().level().getEntitiesOfClass(
                BudgerigarEntity.class,
                bird().getBoundingBox().inflate(goalDatum.flockSearchRange()),
                e -> e != bird() && !e.isPassenger()
        );

        if (flock.size() < goalDatum.flockMinSize()) {
            return false;
        }

        // 计算 flock 中心
        Vec3 center = Vec3.ZERO;
        for (BudgerigarEntity member : flock) {
            center = center.add(member.position());
        }
        center = center.scale(1.0 / flock.size());

        // 选择一个靠近中心的目标
        double distance = bird().distanceToSqr(center);
        if (distance < goalDatum.flockTargetRange()) {
            return false;
        }

        this.flockTarget = center.add(
                (bird().getRandom().nextDouble() - 0.5) * goalDatum.flockRange(),
                0,
                (bird().getRandom().nextDouble() - 0.5) * goalDatum.flockRange()
        );
        return true;
    }


    @Override
    public boolean onContinue() {
        return this.flockTarget != null && bird().distanceToSqr(this.flockTarget) > goalDatum().flockLostRange();
    }

    @Override
    public void onStart() {
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.IDLE
                || bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.WALKING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
        }
    }

    @Override
    public void onReset() {
        if (this.flockTarget == null) {
            return;
        }
        bird().getNavigation().moveTo(this.flockTarget.x, this.flockTarget.y, this.flockTarget.z, goalDatum().flockMoveSpeed());

    }

    @Override
    public void onStop() {
        this.flockTarget = null;
    }
}
