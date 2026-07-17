package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;

public class BirdRandomLookAroundGoal extends AbstractBirdGoal {

    private double relX;
    private double relZ;

    public BirdRandomLookAroundGoal(AbstractBirdEntity<?> bird) {
        super(bird, 5);
    }

    @Override
    protected boolean usePredicates() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    protected boolean defaultContinuePredicates() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    protected void onStart() {
        double d0 = (Math.PI * 2D) * bird().getRandom().nextDouble();
        this.relX = Math.cos(d0);
        this.relZ = Math.sin(d0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    protected void reset() {
        setRepathTicksWithVariance(goalDatum().randomLookAroundTicks(), goalDatum().randomLookAroundTicksVariance());
    }

    @Override
    public void onTick() {
        bird().getLookControl().setLookAt( bird().getX() + this.relX, bird().getEyeY(), bird().getZ() + this.relZ);
    }
}
