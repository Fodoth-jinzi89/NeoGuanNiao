package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;
import net.minecraft.world.phys.Vec3;

public class BirdSentinelGoal extends AbstractBirdGoal {
    public BirdSentinelGoal(AbstractBirdEntity<?> bird) {
        super(bird, 120);
    }

    @Override
    protected boolean usePredicates() {
        return defaultAdditionalPredicates();
    }

    @Override
    protected boolean continuePredicates() {
        return false;
    }

    @Override
    protected void onStart() {
        bird().getTickController().getTickTimer().getBirdSentinelTicker().setTicksWithVariance(goalDatum().sentinelTicks(), goalDatum().sentinelTicks());
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.SENTINEL);
        bird().getNavigation().stop();

        // 看向随机方向
        Vec3 lookAt = new Vec3(
                bird().getX() + bird().getRandom().nextGaussian() * goalDatum().sentinelLookXVariance(),
                bird().getY(),
                bird().getZ() + bird().getRandom().nextGaussian() * goalDatum().sentinelLookZVariance()
        );
        bird().getLookControl().setLookAt(lookAt.x, lookAt.y, lookAt.z, goalDatum().sentinelLookYaw(), goalDatum().sentinelLookPitch());
    }
}
