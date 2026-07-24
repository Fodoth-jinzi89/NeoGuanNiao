package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.phys.Vec3;

public class BirdSentinelGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {

    @Override
    public int chance() {
        return goalDatum().randomLookAroundChance();
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public void onStart() {
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

    @Override
    public void onStop() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
    }
}
