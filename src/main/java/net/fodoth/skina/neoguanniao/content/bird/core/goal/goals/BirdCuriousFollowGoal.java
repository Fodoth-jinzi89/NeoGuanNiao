package net.fodoth.skina.neoguanniao.content.bird.core.goal.goals;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.AbstractBirdGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class BirdCuriousFollowGoal extends AbstractBirdGoal {
    private LivingEntity target;

    public BirdCuriousFollowGoal(AbstractBirdEntity<?> bird) {
        super(bird, 20);
    }

    @Override
    protected boolean usePredicates() {

        if (!bird().getGoalController().canStartSocialGoal() || bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning()) {
            return false;
        }

        // 寻找附近感兴趣的生物
        // TODO 标签？配置项？
        AABB searchBox = bird().getBoundingBox().inflate(bird().getBirdData().goal().curiousFollowSearchRange());
        for (LivingEntity e : bird().level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (e instanceof Player player) {
                if (!player.isSpectator() && player.isAlive()) {
                    this.target = player;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected boolean continuePredicates() {
        return this.target != null && this.target.isAlive() && bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning()
                && bird().getGoalController().canStartSocialGoal()
                && bird().distanceToSqr(this.target) < bird().getBirdData().goal().curiousFollowLostRange();
    }

    @Override
    protected void onStart() {
        var curiousTicker = bird().getTickController().getTickTimer().getBirdCuriousTicker();
        curiousTicker.setTicks(goalDatum().curiousTicks() + bird().getRandom().nextInt(goalDatum().curiousTicksVariance()));
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.CURIOUS);
        bird().getNavigation().stop();
    }

    @Override
    protected void onTick() {
        if (this.target == null) {
            return;
        }
        bird().getLookControl().setLookAt(this.target, goalDatum().curiousLookYaw(), goalDatum().curiousLookPitch());
        double distance = bird().distanceToSqr(this.target);

        if (distance > goalDatum().curiousFollowSneakRange()) {
            if (bird().getRandom().nextFloat() < goalDatum().curiousSneakChance()) {
                bird().getNavigation().moveTo(this.target, goalDatum().curiousFollowSneakSpeed());
            } else {
                bird().getNavigation().stop();
            }
        }
    }


    @Override
    protected void onReset() {
        if (this.target == null) {
            return;
        }
        double distance = bird().distanceToSqr(this.target);
        if (distance > goalDatum().curiousFollowWalkRange()) {
            bird().getNavigation().moveTo(this.target, goalDatum().curiousFollowWalkSpeed());
        }
    }

    @Override
    protected void onStop() {
        this.target = null;
        bird().getNavigation().stop();
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.CURIOUS) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }

}
