package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;

public class BudgerigarEatFoodGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private ItemEntity targetFood;
    private int repathTicks;

    public BudgerigarEatFoodGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.budgerigar.getGoalController().canStartFoodGoal()) {
            return false;
        }

        List<ItemEntity> items = this.budgerigar.level().getEntitiesOfClass(
                ItemEntity.class,
                this.budgerigar.getBoundingBox().inflate(8.0),
                item -> !item.getItem().isEmpty() && this.budgerigar.getEatingController().isEdibleFood(item.getItem())
        );

        if (items.isEmpty()) {
            return false;
        }

        this.targetFood = items.getFirst();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetFood != null && this.targetFood.isAlive()
                && !this.targetFood.getItem().isEmpty()
                && this.budgerigar.getGoalController().canStartFoodGoal();
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.FORAGING);
    }

    @Override
    public void tick() {
        if (this.targetFood == null || !this.targetFood.isAlive()) {
            return;
        }

        double distance = this.budgerigar.distanceToSqr(this.targetFood);

        if (distance < 1.5) {
            this.budgerigar.getEatingController().consumeItemEntity(this.targetFood);
            this.targetFood = null;
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            return;
        }

        if (--this.repathTicks <= 0) {
            this.repathTicks = 10;
            this.budgerigar.getNavigation().moveTo(this.targetFood, 0.8);
        }

        this.budgerigar.getLookControl().setLookAt(this.targetFood, 20.0F, 20.0F);
    }

    @Override
    public void stop() {
        this.targetFood = null;
        this.repathTicks = 0;
        if (this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.FORAGING) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}