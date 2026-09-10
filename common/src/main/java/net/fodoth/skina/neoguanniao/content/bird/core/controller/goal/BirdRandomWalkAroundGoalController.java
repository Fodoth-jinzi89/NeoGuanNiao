package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class BirdRandomWalkAroundGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T>  {

    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    @Override
    public int chance() {
        return goalDatum().randomWalkAroundChance();
    }

    @Override
    public boolean canUse() {
        var state = bird().getBehaviorStateController().getBehaviorState();
        return (state == BirdBehaviorState.IDLE || state == BirdBehaviorState.SENTINEL) && super.canUse() && !bird().hasControllingPassenger();
    }

    @Override
    public boolean onUse() {
        Vec3 vec3 = DefaultRandomPos.getPos(bird(), goalDatum().randomWalkAroundHorizontalRange(),goalDatum().randomWalkAroundVerticalRange());
        if (vec3 == null) {
            return false;
        }
        this.wantedX = vec3.x;
        this.wantedY = vec3.y;
        this.wantedZ = vec3.z;
        return super.onUse();
    }

    @Override
    public boolean canContinue() {
        return !bird().getNavigation().isDone() && !bird().hasControllingPassenger() && defaultAdditionalPredicates();
    }

    @Override
    public void onStart() {
        bird().getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, goalDatum().randomWalkAroundSpeedModifier());
    }

    @Override
    public void onStop() {
        bird().getNavigation().stop();
    }
}
