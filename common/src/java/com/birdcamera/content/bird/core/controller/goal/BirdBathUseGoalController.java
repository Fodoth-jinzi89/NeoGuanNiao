package com.birdcamera.content.bird.core.controller.goal;

import java.util.Optional;
import java.util.UUID;
import com.birdcamera.content.bath.BirdBathAttraction;
import com.birdcamera.content.bath.BirdBathBlockEntity;
import com.birdcamera.content.bath.BirdBathContentType;
import com.birdcamera.content.bath.BirdBathFeedingAnimatable;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BirdBathUseGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   private BirdBathBlockEntity targetBath;
   private boolean consumed;
   private boolean feedingAnimationStarted;
   private int topUseTicks;
   private int mountHopCooldown;
   private int totalTicks;
   private boolean leavingBath;
   private int leaveCooldown;
   private Vec3 bathStandPosition;

   @Override
   public int chance() {
      return this.goalDatum().bathUseChance();
   }

   @Override
   public boolean canUse() {
      return this.isUsingBath() ? false : !this.bird().isBaby() && this.bird().getGoalController().getBirdEatFoodGoalController().canUse();
   }

   @Override
   public boolean onUse() {
      Optional<BirdBathBlockEntity> found = BirdBathAttraction.findNearbyUsableBath(
         this.bird().level(), this.bird().blockPosition(), this.goalDatum().bathUseSearchRange(), this::canUseBath
      );
      if (found.isEmpty()) {
         return false;
      } else {
         BirdBathBlockEntity bath = found.get();
         if (!BirdBathAttraction.tryClaimUse(bath, this.bird(), this.goalDatum().bathUseClaimTicks())) {
            return false;
         } else {
            this.targetBath = bath;
            this.bathStandPosition = BirdBathAttraction.edgeStandPosition(
               this.targetBath, this.bird().position().add(0.0, (double)this.bird().getBbHeight() * 0.5, 0.0)
            );
            return true;
         }
      }
   }

   private boolean canUseBath(BirdBathBlockEntity bath) {
      if (bath != null && !bath.isRemoved() && this.bird().isAlive() && bath.getLevel() == this.bird().level()) {
         UUID uuid = this.bird().getUUID();
         return this.canUseBathPredicates(bath) && (!bath.isOccupied() || bath.isOccupiedBy(uuid));
      } else {
         return false;
      }
   }

   public boolean canUseBathPredicates(BirdBathBlockEntity bath) {
      return BirdBathAttraction.isAttractiveToSmallSeedBird(bath);
   }

   @Override
   public boolean canContinue() {
      return this.leavingBath
         ? true
         : !this.consumed
            && this.bathExists()
            && this.totalTicks < this.goalDatum().bathUseTotalTicks()
            && this.canUseBath(this.targetBath)
            && (
               this.targetBath.isOccupiedBy(this.bird().getUUID())
                  || BirdBathAttraction.tryClaimUse(this.targetBath, this.bird(), this.goalDatum().bathUseClaimTicks())
            );
   }

   @Override
   public void onStart() {
      this.consumed = false;
      this.topUseTicks = 0;
      this.mountHopCooldown = 0;
      this.totalTicks = 0;
      this.feedingAnimationStarted = false;
      if (this.bathExists()) {
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.BATHING);
         this.moveToBathTop();
      }
   }

   public boolean bathExists() {
      return this.targetBath != null;
   }

   public boolean isRunning() {
      return this.totalTicks > 0;
   }

   @Override
   public void onTick() {
      if (this.targetBath != null) {
         if (this.leavingBath) {
            if (this.leaveCooldown > 0) {
               this.leaveCooldown--;
            } else if (this.bird().onGround()) {
               this.jumpDownFromBath();
            }
         } else {
            this.totalTicks++;
            if (this.mountHopCooldown > 0) {
               this.mountHopCooldown--;
            }

            Vec3 usePosition = BirdBathAttraction.topUsePosition(this.targetBath);
            this.bird()
               .getLookControl()
               .setLookAt(usePosition.x, usePosition.y, usePosition.z, this.goalDatum().bathUseLookYaw(), this.goalDatum().bathUseLookPitch());
            if (this.isAtTopUsePosition()) {
               this.bird().getNavigation().stop();
               this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.USING_BATH);
               this.topUseTicks++;
               this.startFeedingAnimationIfNeeded();
               if (this.topUseTicks >= this.goalDatum().bathUseConsumeWarmUpTicks()) {
                  this.consumeFromBath();
               }
            } else {
               this.topUseTicks = 0;
               this.feedingAnimationStarted = false;
               if (this.shouldTick() && this.totalTicks % this.goalDatum().bathUseTryClaimChance() == 0) {
                  BirdBathAttraction.tryClaimUse(this.targetBath, this.bird(), this.goalDatum().bathUseClaimTicks());
                  this.onReset();
               }
            }
         }
      }
   }

   @Override
   public boolean shouldTick() {
      return this.leavingBath ? true : !this.trySettleOntoTop(this.bathStandPosition) && !this.tryHopOntoTop(this.bathStandPosition) || this.isUsingBath();
   }

   @Override
   public void onReset() {
      if (this.bird().getNavigation().isDone()) {
         this.moveToBathTop();
      }
   }

   @Override
   public void onStop() {
      BirdBathBlockEntity bath = this.targetBath;
      if (bath != null) {
         bath.releaseUse(this.bird().getUUID());
         BirdBehaviorState state = this.bird().getBehaviorStateController().getBehaviorState();
         if (state == BirdBehaviorState.BATHING || state == BirdBehaviorState.USING_BATH) {
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
         }
      }

      this.targetBath = null;
      this.bathStandPosition = null;
      this.topUseTicks = 0;
      this.mountHopCooldown = 0;
      this.totalTicks = 0;
      this.leavingBath = false;
      this.leaveCooldown = 0;
      this.feedingAnimationStarted = false;
      this.consumed = false;
   }

   private void moveToBathTop() {
      if (this.targetBath != null) {
         Vec3 top = BirdBathAttraction.topStandPosition(this.targetBath);
         this.bird().getNavigation().moveTo(top.x, top.y, top.z, this.goalDatum().bathUseSpeedModifier());
      }
   }

   private boolean tryHopOntoTop(Vec3 standPosition) {
      if (this.targetBath != null && this.mountHopCooldown <= 0) {
         Vec3 feet = this.bird().position();
         double horizontalDistanceSqr = feet.subtract(standPosition).multiply(1.0, 0.0, 1.0).lengthSqr();
         double feetOffset = feet.y - (double)this.targetBath.getBlockPos().getY();
         if (!(feetOffset >= this.goalDatum().bathUseTopMinYFeetOffset()) && !(horizontalDistanceSqr > this.goalDatum().bathUseTopMountRangeSqr())) {
            if (this.bird().startBirdBathMountFlight(standPosition)) {
               this.mountHopCooldown = this.goalDatum().bathUseMountHopCooldown();
               return true;
            } else {
               Vec3 horizontal = standPosition.subtract(feet).multiply(1.0, 0.0, 1.0);
               if (horizontal.lengthSqr() <= 1.0E-4) {
                  horizontal = Vec3.ZERO;
               } else {
                  horizontal = horizontal.normalize().scale(this.goalDatum().bathUseHopHorizontalScale());
               }

               this.bird().getNavigation().stop();
               this.bird().setDeltaMovement(horizontal.x, this.goalDatum().bathUseHopVerticalScale(), horizontal.z);
               this.bird().fallDistance = 0.0F;
               this.mountHopCooldown = this.goalDatum().bathUseMountHopCooldown();
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean trySettleOntoTop(Vec3 standPosition) {
      if (this.targetBath == null) {
         return false;
      } else {
         Vec3 feet = this.bird().position();
         double horizontalDistanceSqr = feet.subtract(standPosition).multiply(1.0, 0.0, 1.0).lengthSqr();
         double feetOffset = feet.y - (double)this.targetBath.getBlockPos().getY();
         if (!(horizontalDistanceSqr > this.goalDatum().bathUseTopSettleHorizontalSqr())
            && !(feetOffset < this.goalDatum().bathUseTopSettleMinYFeetOffset())
            && !(feetOffset > this.goalDatum().bathUseTopMaxYFeetOffset())) {
            this.bird().getNavigation().stop();
            this.bird().setPos(standPosition.x, standPosition.y, standPosition.z);
            this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.USING_BATH);
            this.bird().setDeltaMovement(Vec3.ZERO);
            this.bird().fallDistance = 0.0F;
            this.mountHopCooldown = this.goalDatum().bathUseMountHopCooldownShort();
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean isAtTopUsePosition() {
      Vec3 feet = this.bird().position();
      double horizontalDistanceSqr = feet.subtract(this.bathStandPosition).multiply(1.0, 0.0, 1.0).lengthSqr();
      double feetOffset = feet.y - (double)this.targetBath.getBlockPos().getY();
      return horizontalDistanceSqr <= this.goalDatum().bathUseTopHorizontalUseSqr()
         && feetOffset >= this.goalDatum().bathUseTopMinYFeetOffset()
         && feetOffset <= this.goalDatum().bathUseTopMaxYFeetOffset();
   }

   private void startFeedingAnimationIfNeeded() {
      if (!this.feedingAnimationStarted && this.targetBath != null) {
         this.feedingAnimationStarted = true;
         AbstractBirdEntity var2 = this.bird();
         if (var2 instanceof BirdBathFeedingAnimatable) {
            var2.startBirdBathFeedingAnimation(this.targetBath.getContentType(), this.goalDatum().bathUseConsumeWarmUpTicks());
         }
      }
   }

   private void consumeFromBath() {
      if (this.targetBath != null && !this.consumed) {
         BirdBathContentType consumedType = this.targetBath.getContentType();
         if (BirdBathAttraction.consumeServingForBird(this.targetBath)) {
            this.consumed = true;
            if (this.bird().getRandom().nextFloat() < this.goalDatum().bathUseJumpDownChance()) {
               this.leavingBath = true;
               this.leaveCooldown = this.goalDatum().bathUseJumpDownTicks();
            } else {
               this.leavingBath = false;
            }

            this.bird().getBirdControllers().getBirdEatingController().consumeBirdBathServing(this.targetBath, consumedType);
         }
      }
   }

   public boolean isUsingBath() {
      BlockPos pos = this.bird().blockPosition();
      BlockPos belowPos = this.bird().blockPosition().below();
      if (this.bird().level().getBlockEntity(pos) instanceof BirdBathBlockEntity || this.bird().level().getBlockEntity(belowPos) instanceof BirdBathBlockEntity
         )
       {
         return true;
      } else if (this.targetBath == null) {
         return false;
      } else {
         BirdBehaviorState state = this.bird().getBehaviorStateController().getBehaviorState();
         return state == BirdBehaviorState.BATHING || state == BirdBehaviorState.USING_BATH;
      }
   }

   private void jumpDownFromBath() {
      if (this.targetBath != null) {
         Vec3 pos = this.bird().position();
         Vec3 center = Vec3.atCenterOf(this.targetBath.getBlockPos());
         Vec3 direction = pos.subtract(center).multiply(1.0, 0.0, 1.0);
         if (direction.lengthSqr() < 1.0E-4) {
            float yaw = this.bird().getRandom().nextFloat() * (float) (Math.PI * 2);
            direction = new Vec3(Math.cos((double)yaw), 0.0, Math.sin((double)yaw));
         } else {
            direction = direction.normalize();
         }

         float yaw = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
         this.bird().setYRot(yaw);
         this.bird().yBodyRot = yaw;
         this.bird().yHeadRot = yaw;
         this.bird().getNavigation().stop();
         Vec3 jump = direction.scale(this.goalDatum().bathUseHopHorizontalScale() * 0.5);
         this.bird().setDeltaMovement(jump.x, this.goalDatum().bathUseHopVerticalScale() * 0.5, jump.z);
         this.bird().fallDistance = 0.0F;
         this.bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
         this.leavingBath = false;
      }
   }
}
