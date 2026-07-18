package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class BirdCuriousFollowGoalController<T extends AbstractBirdEntity<T>> extends AbstractGoalController<T> {
    private LivingEntity curiousFollowTarget;

    @Override
    public int chance() {
        return goalDatum().curiousFollowChance();
    }

    @Override
    public boolean canUse() {
        return canStartSocialGoal() && bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning();
    }

    @Override
    public boolean onUse() {

        // 寻找附近感兴趣的生物
        // TODO 标签？配置项？
        AABB searchBox = bird().getBoundingBox().inflate(bird().getBirdData().goal().curiousFollowSearchRange());
        for (LivingEntity e : bird().level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (e instanceof Player player) {
                if (!player.isSpectator() && player.isAlive()) {
                    setCuriousFollowTarget(player);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinue() {
        return getCuriousFollowTarget() != null && getCuriousFollowTarget().isAlive() && bird().getTickController().getTickTimer().getBirdCuriousTicker().isRunning()
                && canStartSocialGoal()
                && bird().distanceToSqr(getCuriousFollowTarget()) < bird().getBirdData().goal().curiousFollowLostRange();
    }

    /**
     * 判断是否可以开始社交目标
     *
     * @return 如果可以开始社交返回 true
     */
    public boolean canStartSocialGoal() {
        var routineController = bird.getRoutineController();
        var eatingController = bird.getEatingController();
        var stateController = bird.getBehaviorStateController();

        boolean isActiveTime = routineController.isActiveTime();
        boolean isNotEating = !eatingController.isEating();
        boolean isNotSleepingOrRoosting = !routineController.isSleepingOrRoosting();
        boolean isNotEscaping = !stateController.getBehaviorState().isEscape();

        return isActiveTime && isNotEating && isNotSleepingOrRoosting && isNotEscaping;
    }

    @Override
    public void onStart() {
        var curiousTicker = bird().getTickController().getTickTimer().getBirdCuriousTicker();
        curiousTicker.setTicks(goalDatum().curiousTicks() + bird().getRandom().nextInt(goalDatum().curiousTicksVariance()));
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.CURIOUS);
        bird().getNavigation().stop();
    }

    @Override
    public void onTick() {
        if (getCuriousFollowTarget() == null) {
            return;
        }
        bird().getLookControl().setLookAt(getCuriousFollowTarget(), goalDatum().curiousLookYaw(), goalDatum().curiousLookPitch());
        double distance = bird().distanceToSqr(getCuriousFollowTarget());

        if (distance > goalDatum().curiousFollowSneakRange()) {
            if (bird().getRandom().nextFloat() < goalDatum().curiousSneakChance()) {
                bird().getNavigation().moveTo(getCuriousFollowTarget(), goalDatum().curiousFollowSneakSpeed());
            } else {
                bird().getNavigation().stop();
            }
        }
    }

    @Override
    public void onReset() {
        if (getCuriousFollowTarget() == null) {
            return;
        }
        double distance = bird().distanceToSqr(getCuriousFollowTarget());
        if (distance > goalDatum().curiousFollowWalkRange()) {
            bird().getNavigation().moveTo(getCuriousFollowTarget(), goalDatum().curiousFollowWalkSpeed());
        }
    }

    @Override
    public void onStop() {
        setCuriousFollowTarget(null);
        bird().getNavigation().stop();
        if (bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.CURIOUS) {
            bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        }
    }


    public LivingEntity getCuriousFollowTarget() {
        return curiousFollowTarget;
    }

    public void setCuriousFollowTarget(LivingEntity curiousFollowTarget) {
        this.curiousFollowTarget = curiousFollowTarget;
    }




}
