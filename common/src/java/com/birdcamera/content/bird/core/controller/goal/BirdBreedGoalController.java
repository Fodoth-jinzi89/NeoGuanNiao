package com.birdcamera.content.bird.core.controller.goal;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class BirdBreedGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Nullable
   private AbstractBirdEntity<?> partner;
   private int loveTime;

   @Override
   public int chance() {
      return this.goalDatum().breedChance();
   }

   @Override
   public boolean canUse() {
      return super.canUse() && this.bird().isInLove();
   }

   @Override
   public boolean onUse() {
      return this.findPartner();
   }

   private boolean isPartnerValidForBreeding() {
      return this.partner != null
         && this.partner.isAlive()
         && this.partner.isInLove()
         && this.partner.getRoutineController().isActiveTime()
         && !this.partner.isPanicking()
         && !this.partner.getEatingController().isEating()
         && !this.partner.isDancing()
         && !this.partner.getRoutineController().isSleepingOrRoosting()
         && !this.partner.getBehaviorStateController().getBehaviorState().isEscape()
         && this.bird().distanceToSqr(this.partner) < this.goalDatum().breedPartnerLostRange();
   }

   @Override
   public boolean canContinue() {
      return super.canContinue() && this.isPartnerValidForBreeding() && this.loveTime < this.goalDatum().breedMaxLoveTime();
   }

   public boolean shouldBreed() {
      return this.isPartnerValidForBreeding() && this.loveTime >= this.goalDatum().breedMaxLoveTime();
   }

   @Override
   public void onStart() {
      this.loveTime = 0;
   }

   @Override
   public void onTick() {
      if (this.partner != null) {
         this.bird().getLookControl().setLookAt(this.partner, this.goalDatum().breedLookYaw(), (float)this.bird().getMaxHeadXRot());
         double distance = this.bird().distanceToSqr(this.partner);
         if (distance > 0.8 * this.goalDatum().breedDistance()) {
            this.bird().getNavigation().moveTo(this.partner, this.goalDatum().breedMoveSpeed());
         }

         if (!(distance > this.goalDatum().breedDistance())) {
            this.bird().getNavigation().stop();
            if (this.bird().level() instanceof ServerLevel serverLevel) {
               serverLevel.sendParticles(
                  ParticleTypes.HEART,
                  this.bird().getX(),
                  this.bird().getY() + this.goalDatum().breedHeartParticleYOffset(),
                  this.bird().getZ(),
                  1,
                  0.2,
                  0.2,
                  0.2,
                  0.5
               );
            }

            this.loveTime++;
            if (this.shouldBreed()) {
               this.breed();
            }
         }
      }
   }

   @Override
   public void onStop() {
      this.partner = null;
      this.loveTime = 0;
   }

   public void breed() {
      if (this.partner != null) {
         AbstractBirdEntity<?> female = this.bird().isMale() ? this.partner : this.bird();
         AbstractBirdEntity<?> male = this.bird().isMale() ? this.bird() : this.partner;
         female.spawnChildFromBreeding((ServerLevel)female.level(), male);
      }
   }

   private boolean findPartner() {
      var nearby = this.bird()
         .level()
         .getNearbyEntities(
            (Class<? extends AbstractBirdEntity<?>>)(Class<?>)this.bird().getClass(),
            TargetingConditions.forNonCombat().range(this.goalDatum().breedPartnerTargetingRange()).ignoreLineOfSight(),
            this.bird(),
            this.bird().getBoundingBox().inflate(this.goalDatum().breedPartnerTargetingRange())
         );
      double closestDistance = Double.MAX_VALUE;
      AbstractBirdEntity<?> closest = null;

      for (AbstractBirdEntity<?> candidate : nearby) {
         if (this.bird().canMate(candidate) && !candidate.isPanicking() && this.bird().isMale() != candidate.isMale()) {
            double distance = this.bird().distanceToSqr(candidate);
            if (distance < closestDistance) {
               closestDistance = distance;
               closest = candidate;
            }
         }
      }

      this.partner = closest;
      return closest != null;
   }

   @Nullable
   public AbstractBirdEntity<?> getPartner() {
      return this.partner;
   }

   public int getLoveTime() {
      return this.loveTime;
   }
}
