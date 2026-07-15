package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.ColumbidBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

public class ColumbidEatSeedGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private ItemEntity targetItem;
    private int repathTicks;
    private int peckTicks;

    public ColumbidEatSeedGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.canStartSeedGoal()) {
            return false;
        }
        this.targetItem = this.findSeed();
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive()
                && AbstractColumbidEntity.isSeedFood(this.targetItem.getItem())
                && this.columbid.canStartSeedGoal()
                && this.columbid.distanceToSqr(this.targetItem) < 196.0;
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.peckTicks = 8;
        this.columbid.setBehaviorState(ColumbidBehaviorState.FORAGING);
    }

    @Override
    public void tick() {
        this.columbid.getLookControl().setLookAt(this.targetItem, 20.0F, 20.0F);
        double distanceSqr = this.columbid.distanceToSqr(this.targetItem);

        if (distanceSqr > 1.7) {
            if (--this.repathTicks <= 0) {
                this.repathTicks = 10;
                this.columbid.getNavigation().moveTo(this.targetItem, 0.88);
                this.columbid.setBehaviorState(ColumbidBehaviorState.FORAGING);
            }
        } else {
            this.columbid.getNavigation().stop();
            if (--this.peckTicks <= 0) {
                ItemStack stack = this.targetItem.getItem();
                if (AbstractColumbidEntity.isSeedFood(stack)) {
                    boolean preferredSeed = AbstractColumbidEntity.isPreferredTamingSeed(stack);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        this.targetItem.discard();
                    } else {
                        this.targetItem.setItem(stack);
                    }
                    this.columbid.seedTrustTicks = Math.max(this.columbid.seedTrustTicks, 900);
                    this.columbid.birdBrain().onEat(preferredSeed ? 0.28F : 0.18F);
                    this.columbid.triggerEatingAnimation(30);
                }
                this.peckTicks = 18 + this.columbid.getRandom().nextInt(16);
            }
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.FORAGING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }

    private ItemEntity findSeed() {
        List<ItemEntity> items = this.columbid.level().getEntitiesOfClass(
                ItemEntity.class,
                this.columbid.getBoundingBox().inflate(10.0, 3.0, 10.0),
                item -> item.isAlive() && AbstractColumbidEntity.isSeedFood(item.getItem())
        );

        ItemEntity best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (ItemEntity item : items) {
            double score = -this.columbid.distanceToSqr(item);
            if (AbstractColumbidEntity.isPreferredTamingSeed(item.getItem())) {
                score += 18.0;
            }
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        return best;
    }
}