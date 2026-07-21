package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import javax.annotation.Nullable;
import java.util.List;

public class BirdBreedGoalController<T extends AbstractBirdEntity<?>>
        extends AbstractGoalController<T> {

    @Nullable
    private AbstractBirdEntity<?> partner;

    private int loveTime;

    @Override
    public int chance() {
        return goalDatum().breedChance();
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && bird().isInLove();
    }

    @Override
    public boolean onUse() {
        return findPartner();
    }

    private boolean isPartnerValidForBreeding() {
        return partner != null
                && partner.isAlive()
                && partner.isInLove()
                && partner.getRoutineController().isActiveTime()
                && !partner.isPanicking()
                && !partner.getEatingController().isEating()
                && !partner.isDancing()
                && !partner.getRoutineController().isSleepingOrRoosting()
                && !partner.getBehaviorStateController().getBehaviorState().isEscape()
                && bird().distanceToSqr(partner) < goalDatum().breedPartnerLostRange();
    }

    @Override
    public boolean canContinue() {
        return super.canContinue()
                && isPartnerValidForBreeding()
                && loveTime < goalDatum().breedMaxLoveTime();
    }

    public boolean shouldBreed() {
        return isPartnerValidForBreeding()
                && loveTime >= goalDatum().breedMaxLoveTime();
    }

    @Override
    public void onStart() {
        loveTime = 0;
    }

    @Override
    public void onTick() {
        if (partner == null) {
            return;
        }

        bird().getLookControl().setLookAt(
                partner,
                goalDatum().breedLookYaw(),
                bird().getMaxHeadXRot()
        );

        double distance = bird().distanceToSqr(partner);

        // 距离太远，主动靠近
        if (distance > 0.8 * goalDatum().breedDistance()) {

            bird().getNavigation().moveTo(
                    partner,
                    goalDatum().breedMoveSpeed()
            );

        }

        if (distance > goalDatum().breedDistance()) {
            return;
        }

        // 足够近才增加loveTime
        bird().getNavigation().stop();

        if (bird().level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    bird().getX(),
                    bird().getY() + goalDatum().breedHeartParticleYOffset(),
                    bird().getZ(),
                    1,
                    0.2,
                    0.2,
                    0.2,
                    0.5
            );
        }

        loveTime++;

        if (shouldBreed()) {
            breed();
        }
    }

    @Override
    public void onStop() {
        partner = null;
        loveTime = 0;
    }

    public void breed() {
        if (partner == null) {
            return;
        }

        AbstractBirdEntity<?> female = bird().isMale() ? partner : bird();
        AbstractBirdEntity<?> male = bird().isMale() ? bird() : partner;

        female.spawnChildFromBreeding(
                (ServerLevel) female.level(),
                male
        );
    }


    @SuppressWarnings("unchecked")
    private boolean findPartner() {

        List<? extends AbstractBirdEntity<?>> nearby =
                (List<? extends AbstractBirdEntity<?>>)
                        bird().level().getNearbyEntities(
                                bird().getClass(),
                                TargetingConditions.forNonCombat()
                                        .range(goalDatum().breedPartnerTargetingRange())
                                        .ignoreLineOfSight(),
                                bird(),
                                bird().getBoundingBox().inflate(
                                        goalDatum().breedPartnerTargetingRange()
                                )
                        );

        double closestDistance = Double.MAX_VALUE;
        AbstractBirdEntity<?> closest = null;

        for (AbstractBirdEntity<?> candidate : nearby) {

            if (!bird().canMate(candidate)) {
                continue;
            }

            if (candidate.isPanicking()) {
                continue;
            }

            // 禁止同性
            if (bird().isMale() == candidate.isMale()) {
                continue;
            }

            double distance = bird().distanceToSqr(candidate);

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = candidate;
            }
        }

        partner = closest;
        return closest != null;
    }

    @Nullable
    public AbstractBirdEntity<?> getPartner() {
        return partner;
    }

    public int getLoveTime() {
        return loveTime;
    }
}