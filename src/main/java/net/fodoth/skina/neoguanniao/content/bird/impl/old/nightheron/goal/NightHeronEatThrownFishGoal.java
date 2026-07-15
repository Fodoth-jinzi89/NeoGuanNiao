package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * 夜鹭吃投掷的鱼目标
 * 控制夜鹭寻找并吃掉玩家投掷的鱼物品
 */
public class NightHeronEatThrownFishGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private ItemEntity targetFish;
    private int eatDelayTicks;

    public NightHeronEatThrownFishGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.nightHeron.canEatThrownFish()) {
            return false;
        }
        this.targetFish = this.findNearestFish();
        return this.targetFish != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.nightHeron.isEatingFish()) {
            return true;
        }
        return this.targetFish != null && this.targetFish.isAlive()
                && NightHeronEntity.isEdibleFishItem(this.targetFish.getItem())
                && this.nightHeron.canEatThrownFish()
                && this.nightHeron.distanceToSqr(this.targetFish) < 324.0;
    }

    @Override
    public void start() {
        this.eatDelayTicks = 0;
        if (!this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.FORAGING);
        }
    }

    @Override
    public void tick() {
        if (this.nightHeron.isEatingFish()) {
            this.nightHeron.getNavigation().stop();
            return;
        }

        if (this.targetFish == null || !this.targetFish.isAlive()
                || !NightHeronEntity.isEdibleFishItem(this.targetFish.getItem())) {
            this.targetFish = this.findNearestFish();
            this.eatDelayTicks = 0;
            if (this.targetFish == null) {
                return;
            }
        }

        this.nightHeron.getLookControl().setLookAt(this.targetFish, 30.0F, 30.0F);
        double distanceSqr = this.nightHeron.distanceToSqr(this.targetFish);

        if (distanceSqr > 2.56) {
            this.eatDelayTicks = 0;
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.FORAGING);
            this.nightHeron.getNavigation().moveTo(this.targetFish, 1.0);
        } else {
            this.nightHeron.getNavigation().stop();
            if (++this.eatDelayTicks >= 6) {
                this.nightHeron.eatThrownFish(this.targetFish);
                this.targetFish = null;
            }
        }
    }

    @Override
    public void stop() {
        this.targetFish = null;
        this.eatDelayTicks = 0;
        if (!this.nightHeron.isEatingFish() && this.nightHeron.getBehaviorState() == NightHeronBehaviorState.FORAGING) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    /**
     * 查找最近的鱼物品
     */
    private ItemEntity findNearestFish() {
        return this.nightHeron.level().getEntitiesOfClass(
                        ItemEntity.class,
                        this.nightHeron.getBoundingBox().inflate(12.0, 3.0, 12.0),
                        itemEntity -> itemEntity.isAlive() && NightHeronEntity.isEdibleFishItem(itemEntity.getItem())
                ).stream()
                .min(Comparator.comparingDouble(this.nightHeron::distanceToSqr))
                .orElse(null);
    }
}