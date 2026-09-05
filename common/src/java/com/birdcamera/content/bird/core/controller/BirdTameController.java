package com.birdcamera.content.bird.core.controller;

import java.util.UUID;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdTameDatum;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BirdTameController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private static final byte EVENT_TAME_FAILED = 6;
   private static final byte EVENT_TAME_SUCCESS = 7;
   private UUID interestedPlayerUUID;

   public void broadcastTameEvent(boolean tame) {
      byte event = (byte)(tame ? 7 : 6);
      this.bird.level().broadcastEntityEvent(this.bird, event);
   }

   public void handleTameEvent(byte id) {
      if (id == 7) {
         this.bird.setTame(true, true);
      } else if (id == 6) {
         this.bird.setTame(false, true);
      }
   }

   public void checkTame(Player player, ItemStack eaten, int addTrust, int addTrustNearby, boolean shouldTame) {
      BirdTickController<T> tickController = this.bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdEatingController<T> eatingController = this.bird.getEatingController();
      BirdData birdData = this.bird.getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      boolean wasTame = this.bird.isTame();
      this.setInterestedPlayerUUID(player.getUUID());
      eatingController.startEatingFood(eaten);
      int trust = this.bird().isBaby() ? addTrust * 2 : addTrust;
      timer.getBirdTrustTicker().addTrust(trust);
      int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
      int curiousLimit = miscDatum.curiousTicksLimitForTame();
      timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
      eatingController.shareTrustNearby(addTrustNearby);
      if (shouldTame) {
         this.updateTrustedOwner(player);
         if (!wasTame && this.bird.isTame()) {
            this.startTameCelebration(player);
            this.triggerTameSideEffects(player);
            this.broadcastTameEvent(true);
         } else if (!wasTame) {
            this.broadcastTameEvent(false);
         }
      }
   }

   public void triggerTameSideEffects(Player player) {
   }

   public void setInterestedPlayerUUID(UUID interestedPlayerUUID) {
      this.interestedPlayerUUID = interestedPlayerUUID;
   }

   public UUID getInterestedPlayerUUID() {
      return this.interestedPlayerUUID;
   }

   public void updateTrustedOwner(Player player) {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      BirdData birdData = this.bird.getBirdData();
      BirdTameDatum tameDatum = birdData.tame();
      int trustTicks = timer.getBirdTrustTicker().getTicks();
      int tameThreshold = tameDatum.trustTameThreshold();
      if (!this.bird.isTame() && trustTicks >= tameThreshold) {
         this.bird.tame(player);
      } else if (this.bird.isTame() && this.bird.getOwner() == null) {
         this.bird.setOwnerUUID(player.getUUID());
      }
   }

   public void startTameCelebration(Player player) {
      BirdTickController<T> tickController = this.bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdEatingController<T> eatingController = this.bird.getEatingController();
      BirdBehaviorStateController<T> stateController = this.bird.getBehaviorStateController();
      BirdData birdData = this.bird.getBirdData();
      BirdTameDatum tameDatum = birdData.tame();
      RandomSource random = this.bird.getRandom();
      eatingController.clearEating();
      this.bird.getNavigation().stop();
      int postTameActionTicks = tameDatum.tameCelebrationPostTameActionTicksMin() + random.nextInt(tameDatum.tameCelebrationPostTameActionTicksVariance());
      timer.getBirdPostTameActionTicker().setTicks(postTameActionTicks);
      timer.getBirdPostTameActionSwapTicker().setTicks(tameDatum.tameCelebrationPostTameActionSwapTicks());
      int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
      int curiousLimit = tameDatum.tameCelebrationCuriousTicks();
      timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
      timer.getBirdIdleAnimationTicker().setTicks(0);
      int currentFoodTicks = timer.getBirdFoodTicker().getTicks();
      int foodTicks = tameDatum.tameCelebrationFoodTicks();
      timer.getBirdFoodTicker().setTicks(Math.max(currentFoodTicks, foodTicks));
      int behaviorTicks = tameDatum.tameCelebrationBehaviorStateTicks();
      stateController.setBehaviorStateFor(BirdBehaviorState.CURIOUS, behaviorTicks);
      this.bird.getLookControl().setLookAt(player, 35.0F, 35.0F);
   }
}
