package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

public class BirdEatFoodGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
    private ItemEntity targetFood;


    @Override
    public int chance() {
        return goalDatum().eatFoodChance();
    }

    /**
     * 判断是否可以开始觅食目标
     *
     * @return 如果可以开始觅食返回 true
     */
    @Override
    public boolean canUse() {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var eatingController = bird.getEatingController();
        var routineController = bird.getRoutineController();
        var stateController = bird.getBehaviorStateController();

        boolean hasNoFoodTicks = !timer.getBirdFoodTicker().isRunning();
        boolean isNotEating = !eatingController.isEating();
        boolean isNotPassenger = !bird.isPassenger();
        boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
        boolean isNotEscaping = !stateController.getBehaviorState().isEscape();
        boolean isMature = !bird().isBaby();

        return isMature && hasNoFoodTicks && isNotEating && isNotPassenger
                && isNotSleepingOrRoosting && isNotEscaping;
    }

    @Override
    public boolean onUse() {
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
    public boolean canContinue() {
        if (!bird().getAnimationController().shouldPlayFlyAnimation() && !bird().onGround()) {
            this.targetFood = null;
        }

        return this.targetFood != null && this.targetFood.isAlive()
                && !this.targetFood.getItem().isEmpty()
                && canUse();
    }

    @Override
    public void onStart() {
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.FORAGING);
    }

    @Override
    public void onTick() {
        if (this.targetFood == null || !this.targetFood.isAlive()) {
            return;
        }

        double distance = bird().distanceToSqr(this.targetFood);

        if (distance > 0.8 * goalDatum().eatFoodConsumeDistance()) {
            if (bird().getY() >= targetFood.getY()) {
                bird().getNavigation().setCanFloat(false);
            }
            bird().getNavigation().moveTo(this.targetFood, goalDatum().eatFoodMoveSpeed());
        }

        if (distance < goalDatum().eatFoodConsumeDistance()) {
            bird().getNavigation().stop();
            bird().getEatingController().consumeItemEntity(bird(), this.targetFood);
            this.targetFood = null;
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            return;
        }
        bird().getLookControl().setLookAt(this.targetFood, goalDatum().eatFoodLookYaw(), goalDatum().eatFoodLookPitch());

    }

    @Override
    public void onStop() {
        this.targetFood = null;
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FORAGING) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}
