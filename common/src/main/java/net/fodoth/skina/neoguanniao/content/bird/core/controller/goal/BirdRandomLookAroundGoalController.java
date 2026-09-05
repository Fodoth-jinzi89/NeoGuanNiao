package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

public class BirdRandomLookAroundGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {

    private double relX;
    private double relZ;

    @Override
    public int chance() {
        return goalDatum().randomLookAroundChance();
    }

    @Override
    public boolean canUse() {
        return !bird().getRoutineController().isSleeping();
    }

    @Override
    public void onStart() {
        double d0 = (Math.PI * 2D) * bird().getRandom().nextDouble();
        this.relX = Math.cos(d0);
        this.relZ = Math.sin(d0);
    }

    @Override
    public void onTick() {
        bird().getLookControl().setLookAt( bird().getX() + this.relX, bird().getEyeY(), bird().getZ() + this.relZ);
    }

}
