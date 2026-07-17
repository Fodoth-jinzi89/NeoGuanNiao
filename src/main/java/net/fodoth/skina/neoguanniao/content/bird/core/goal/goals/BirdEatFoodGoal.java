package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

public class BirdEatFoodGoal extends AbstractBirdGoal
{
    private ItemEntity targetFood;

    public BirdEatFoodGoal(AbstractBirdEntity<?> bird) {
        super(bird, 20);
    }

    @Override
    protected boolean usePredicates() {
        if (!bird().getGoalController().canStartFoodGoal()) {
            return false;
        }

        List<ItemEntity> items = bird().level().getEntitiesOfClass(
                ItemEntity.class,
                bird().getBoundingBox().inflate(bird().getBirdData().goal().eatFoodSearchRange()),
                item -> !item.getItem().isEmpty() && bird().getEatingController().isEdibleFood(item.getItem())
        );

        if (items.isEmpty()) {
            return false;
        }

        this.targetFood = items.getFirst();
        return true;
    }

    @Override
    protected boolean continuePredicates() {
        if (!bird().getAnimationController().shouldPlayFlyAnimation() && !bird().onGround()) {
            this.targetFood = null;
        }

        return this.targetFood != null && this.targetFood.isAlive()
                && !this.targetFood.getItem().isEmpty()
                && bird().getGoalController().canStartFoodGoal();
    }

    @Override
    protected void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FORAGING);
    }

    @Override
    protected void onTick() {
        if (this.targetFood == null || !this.targetFood.isAlive()) {
            return;
        }

        double distance = bird().distanceToSqr(this.targetFood);

        if (distance < goalDatum().eatFoodConsumeDistance()) {
            bird().getEatingController().consumeItemEntity(bird(), this.targetFood);
            this.targetFood = null;
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            return;
        }
        bird().getLookControl().setLookAt(this.targetFood, goalDatum().eatFoodLookYaw(), goalDatum().eatFoodLookPitch());

    }

    @Override
    protected void onReset() {
        if (this.targetFood == null || !this.targetFood.isAlive()) {
            return;
        }
        bird().getNavigation().moveTo(this.targetFood, goalDatum().eatFoodMoveSpeed());
    }

    @Override
    protected void onStop() {
        this.targetFood = null;
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FORAGING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}
