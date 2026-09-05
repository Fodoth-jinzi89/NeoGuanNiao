package com.birdcamera.content.bird.core.controller;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdGuidePreviewAnimation;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdAnimationDatum;
import com.birdcamera.content.bird.core.flight.BirdFlightAware;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BirdAnimationController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private RawAnimation currentIdleAnimation;
   private RawAnimation currentSleepAnimation;
   private RawAnimation currentGuideAnimation;
   private AnimatableInstanceCache cache;

   @Override
   protected void onAttach() {
      super.onAttach();
      this.cache = GeckoLibUtil.createInstanceCache(this.bird());
      this.currentIdleAnimation = this.pickIdleAnimation();
      this.currentSleepAnimation = null;
      this.setGuidePreviewAnimation(null);
   }

   public AnimatableInstanceCache cache() {
      if (this.cache == null) {
         throw new IllegalStateException("BirdAnimationController is not attached");
      } else {
         return this.cache;
      }
   }

   public RawAnimation pickIdleAnimation() {
      T bird = this.bird();
      BirdTickController<T> tickController = bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> tickTimer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdData birdData = bird.getBirdData();
      BirdAnimationDatum animationDatum = birdData.animation();
      int idleTicker = tickTimer.getBirdIdleAnimationTicker().getTicks();
      int trustTicker = tickTimer.getBirdTrustTicker().getTicks();
      int curiousTicker = tickTimer.getBirdCuriousTicker().getTicks();
      if (idleTicker <= 0) {
         int randomMax = this.getIdleAnimationRollMax(trustTicker, curiousTicker);
         int roll = bird.getRandom().nextInt(randomMax);
         if (roll <= 1 && !this.shouldUseIdleAnimation(trustTicker, curiousTicker)) {
            int duration = animationDatum.otherDuration() + bird.getRandom().nextInt(animationDatum.otherDurationVariance());
            tickTimer.getBirdIdleAnimationTicker().setTicks(duration);
         } else {
            String selectedKey = this.randomizeIdleAnimationKey();
            this.currentIdleAnimation = animationDatum.animationMap().get(selectedKey);
            int duration;
            if (selectedKey.equals("preen")) {
               duration = animationDatum.preenDuration() + bird.getRandom().nextInt(animationDatum.preenDurationVariance());
            } else {
               duration = animationDatum.idleDuration() + bird.getRandom().nextInt(animationDatum.idleDurationVariance());
            }

            tickTimer.getBirdIdleAnimationTicker().setTicks(duration);
         }
      }

      return this.currentIdleAnimation;
   }

   private String randomizeIdleAnimationKey() {
      BirdAnimationDatum animationDatum = this.bird().getBirdData().animation();
      Map<String, RawAnimation> animationMap = animationDatum.animationMap();
      List<String> idleKeys = animationMap.keySet().stream().filter(key -> key.startsWith("idle") || key.equals("preen")).toList();
      if (idleKeys.isEmpty()) {
         return "idle";
      } else {
         String mainIdleKey = "idle";
         List<String> otherIdleKeys = idleKeys.stream().filter(key -> !key.equals(mainIdleKey)).toList();
         if (this.bird().getRandom().nextFloat() < animationDatum.mainIdleAnimationChance()) {
            return mainIdleKey;
         } else {
            return otherIdleKeys.isEmpty() ? mainIdleKey : otherIdleKeys.get(this.bird().getRandom().nextInt(otherIdleKeys.size()));
         }
      }
   }

   public RawAnimation pickSleepAnimation() {
      if (this.currentSleepAnimation == null) {
         List<RawAnimation> sleepAnimations = this.bird()
            .getBirdData()
            .animation()
            .animationMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().equals("sleep") || entry.getKey().startsWith("sleep_"))
            .filter(entry -> !entry.getKey().equals("sleep_loop"))
            .map(Entry::getValue)
            .toList();
         this.currentSleepAnimation = sleepAnimations.isEmpty()
            ? this.bird().getBirdData().animation().animationMap().get("idle")
            : sleepAnimations.get(this.bird().getRandom().nextInt(sleepAnimations.size()));
      }

      return this.currentSleepAnimation;
   }

   public void resetSleepAnimation() {
      this.currentSleepAnimation = null;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this.bird(), "movement", 4, this.bird()::movementController));
   }

   public boolean shouldPlayFlyAnimation() {
      T bird = this.bird();
      return shouldPlayFlyAnimation(
         bird,
         bird.getBehaviorStateController().getBehaviorState().isAirborne(),
         bird.onGround(),
         bird.isInWater(),
         bird.getDeltaMovement(),
         bird.getBirdData().flying().airborneGraceTicks()
      );
   }

   private static boolean shouldPlayFlyAnimation(
      BirdFlightAware bird, boolean airborneState, boolean onGround, boolean noGravity, Vec3 movement, int airborneGraceTicks
   ) {
      if (bird.isBirdFlightActive() || airborneState) {
         return true;
      } else if (onGround) {
         return false;
      } else if (airborneGraceTicks > 0) {
         return true;
      } else if (!noGravity && !bird.isBirdLanding() && !bird.isBirdEscaping()) {
         return movement.y > -0.85 && Math.abs(movement.y) > 0.001 ? true : movement.lengthSqr() > 0.001;
      } else {
         return true;
      }
   }

   public void setGuidePreviewAnimation(RawAnimation guidePreviewAnimation) {
      this.currentGuideAnimation = guidePreviewAnimation == null ? BirdGuidePreviewAnimation.NONE.animation() : guidePreviewAnimation;
   }

   private int getIdleAnimationRollMax(int trustTicker, int curiousTicker) {
      BirdData birdData = this.bird().getBirdData();
      BirdAnimationDatum animationDatum = birdData.animation();
      boolean isCuriousAndTrusting = trustTicker <= animationDatum.trustTickerMaxLimit() && curiousTicker <= 0;
      return isCuriousAndTrusting ? animationDatum.maxCuriousAndTrustingIndex() : animationDatum.minCuriousAndTrustingIndex();
   }

   private boolean shouldUseIdleAnimation(int trustTicker, int curiousTicker) {
      BirdAnimationDatum animationDatum = this.bird().getBirdData().animation();
      return trustTicker <= animationDatum.trustTickerLimit() && curiousTicker <= 0;
   }

   @Override
   public void tick() {
      super.tick();
   }

   public RawAnimation getCurrentGuideAnimation() {
      return this.currentGuideAnimation;
   }

   public AnimatableInstanceCache getCache() {
      return this.cache;
   }

   public RawAnimation pickFlyAnimation() {
      return this.bird().getBirdData().animation().animationMap().get("fly");
   }
}
