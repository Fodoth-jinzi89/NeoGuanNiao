package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class BudgerigarCuriousFollowGoal extends Goal {
    private final BudgerigarEntity budgerigar;
    private LivingEntity target;
    private int repathTicks;
    private int curiousTicks;

    public BudgerigarCuriousFollowGoal(BudgerigarEntity budgerigar) {
        this.budgerigar = budgerigar;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.budgerigar.getGoalController().canStartSocialGoal() || this.budgerigar.getTickController().getTickTimer().getBirdCuriousTicker().getTicks() <= 0) {
            return false;
        }

        if (this.budgerigar.getRandom().nextInt(20) != 0) {
            return false;
        }

        // 寻找附近的玩家
        AABB searchBox = this.budgerigar.getBoundingBox().inflate(8.0);
        for (Player player : this.budgerigar.level().getEntitiesOfClass(Player.class, searchBox)) {
            if (!player.isSpectator() && player.isAlive()) {
                this.target = player;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.curiousTicks > 0
                && this.budgerigar.getGoalController().canStartSocialGoal()
                && this.budgerigar.distanceToSqr(this.target) < 64.0;
    }

    @Override
    public void start() {
        this.curiousTicks = 60 + this.budgerigar.getRandom().nextInt(60);
        this.repathTicks = 0;
        this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.CURIOUS);
        this.budgerigar.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.curiousTicks--;

        if (this.target == null) {
            return;
        }

        this.budgerigar.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        double distance = this.budgerigar.distanceToSqr(this.target);

        if (distance > 16.0) {
            if (--this.repathTicks <= 0) {
                this.repathTicks = 10;
                this.budgerigar.getNavigation().moveTo(this.target, 0.6);
            }
        } else if (distance > 4.0 && this.budgerigar.getRandom().nextInt(10) == 0) {
            // 慢慢靠近
            this.budgerigar.getNavigation().moveTo(this.target, 0.3);
        } else if (distance <= 4.0) {
            // 靠近后保持距离
            this.budgerigar.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.curiousTicks = 0;
        this.repathTicks = 0;
        this.budgerigar.getNavigation().stop();
        if (this.budgerigar.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.CURIOUS) {
            this.budgerigar.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }
}