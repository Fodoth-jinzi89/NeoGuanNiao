package com.birdcamera.content.bird.core.controller.tick;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.tick.ticker.AbstractBirdTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdBehaviorStateTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdCuriousTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdEatingTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFeatherLoopTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFindNearbyMusicLoopTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFlyingTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFoodTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFrightLoopTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFrightTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdIdleAnimationTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdLandingTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdMusicTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdPendingFrightTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdPostTameActionSwapTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdPostTameActionTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdSentinelTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdTrustTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdUnsafeFloatLoopTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdUnsafeFlyLoopTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.DebugLoopTicker;
import net.fabricmc.loader.api.FabricLoader;

public class BirdTickTimer<T extends AbstractBirdEntity<T>> extends AbstractBirdTimer<T> {
   private final BirdBehaviorStateTicker<T> birdBehaviorStateTicker = new BirdBehaviorStateTicker<>();
   private final BirdCuriousTicker<T> birdCuriousTicker = new BirdCuriousTicker<>();
   private final BirdEatingTicker<T> birdEatingTicker = new BirdEatingTicker<>();
   private final BirdFlyingTicker<T> birdFlyingTicker = new BirdFlyingTicker<>();
   private final BirdFoodTicker<T> birdFoodTicker = new BirdFoodTicker<>();
   private final BirdPendingFrightTicker<T> birdPendingFrightTicker = new BirdPendingFrightTicker<>();
   private final BirdIdleAnimationTicker<T> birdIdleAnimationTicker = new BirdIdleAnimationTicker<>();
   private final BirdPostTameActionSwapTicker<T> birdPostTameActionSwapTicker = new BirdPostTameActionSwapTicker<>();
   private final BirdPostTameActionTicker<T> birdPostTameActionTicker = new BirdPostTameActionTicker<>();
   private final BirdTrustTicker<T> birdTrustTicker = new BirdTrustTicker<>();
   private final BirdFindNearbyMusicLoopTicker<T> birdFindNearbyMusicLoopTicker = new BirdFindNearbyMusicLoopTicker<>();
   private final BirdMusicTicker<T> birdMusicTicker = new BirdMusicTicker<>();
   private final BirdLandingTicker<T> birdLandingTicker = new BirdLandingTicker<>();
   private final BirdUnsafeFlyLoopTicker<T> birdUnsafeFlyLoopTicker = new BirdUnsafeFlyLoopTicker<>();
   private final BirdUnsafeFloatLoopTicker<T> birdUnsafeFloatLoopTicker = new BirdUnsafeFloatLoopTicker<>();
   private final BirdFrightLoopTicker<T> birdFrightLoopTicker = new BirdFrightLoopTicker<>();
   private final BirdFrightTicker<T> birdFrightTicker = new BirdFrightTicker<>();
   private final BirdSentinelTicker<T> birdSentinelTicker = new BirdSentinelTicker<>();
   private final BirdFeatherLoopTicker<T> birdFeatherLoopTicker = new BirdFeatherLoopTicker<>();
   private final DebugLoopTicker<T> debugLoopTicker = new DebugLoopTicker<>();
   private final List<AbstractBirdTicker<T>> tickers;

   public BirdTickTimer() {
      List<AbstractBirdTicker<T>> tickers = new ArrayList<>(
         List.of(
            this.birdBehaviorStateTicker,
            this.birdFlyingTicker,
            this.birdLandingTicker,
            this.birdUnsafeFlyLoopTicker,
            this.birdUnsafeFloatLoopTicker,
            this.birdFrightTicker,
            this.birdPendingFrightTicker,
            this.birdFrightLoopTicker,
            this.birdEatingTicker,
            this.birdFoodTicker,
            this.birdTrustTicker,
            this.birdPostTameActionSwapTicker,
            this.birdPostTameActionTicker,
            this.birdCuriousTicker,
            this.birdSentinelTicker,
            this.birdIdleAnimationTicker,
            this.birdFindNearbyMusicLoopTicker,
            this.birdMusicTicker,
            this.birdFeatherLoopTicker
         )
      );
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         tickers.add(this.debugLoopTicker);
         BirdCameraMod.LOGGER.info("[BirdCameraMod] Dev environment, register DebugLoopTicker");
      }

      this.tickers = List.copyOf(tickers);
   }

   @Override
   protected void onAttach() {
      for (AbstractBirdTicker<T> ticker : this.tickers) {
         ticker.attach(this.bird());
      }
   }

   @Override
   public void tick() {
      for (AbstractBirdTicker<T> ticker : this.tickers) {
         ticker.tick();
      }
   }

   @Override
   public void tickClient() {
      for (AbstractBirdTicker<T> ticker : this.tickers) {
         ticker.tickClient();
      }
   }

   public List<AbstractBirdTicker<T>> getTickers() {
      return this.tickers;
   }

   public BirdBehaviorStateTicker<T> getBirdBehaviorStateTicker() {
      return this.birdBehaviorStateTicker;
   }

   public BirdCuriousTicker<T> getBirdCuriousTicker() {
      return this.birdCuriousTicker;
   }

   public BirdEatingTicker<T> getBirdEatingTicker() {
      return this.birdEatingTicker;
   }

   public BirdFlyingTicker<T> getBirdFlyingTicker() {
      return this.birdFlyingTicker;
   }

   public BirdFoodTicker<T> getBirdFoodTicker() {
      return this.birdFoodTicker;
   }

   public BirdPendingFrightTicker<T> getBirdPendingFrightTicker() {
      return this.birdPendingFrightTicker;
   }

   public BirdIdleAnimationTicker<T> getBirdIdleAnimationTicker() {
      return this.birdIdleAnimationTicker;
   }

   public BirdPostTameActionSwapTicker<T> getBirdPostTameActionSwapTicker() {
      return this.birdPostTameActionSwapTicker;
   }

   public BirdPostTameActionTicker<T> getBirdPostTameActionTicker() {
      return this.birdPostTameActionTicker;
   }

   public BirdTrustTicker<T> getBirdTrustTicker() {
      return this.birdTrustTicker;
   }

   public BirdFindNearbyMusicLoopTicker<T> getBirdFindNearbyMusicLoopTicker() {
      return this.birdFindNearbyMusicLoopTicker;
   }

   public BirdMusicTicker<T> getBirdMusicTicker() {
      return this.birdMusicTicker;
   }

   public BirdLandingTicker<T> getBirdLandingTicker() {
      return this.birdLandingTicker;
   }

   public BirdUnsafeFlyLoopTicker<T> getBirdUnsafeFlyLoopTicker() {
      return this.birdUnsafeFlyLoopTicker;
   }

   public BirdUnsafeFloatLoopTicker<T> getBirdUnsafeFloatLoopTicker() {
      return this.birdUnsafeFloatLoopTicker;
   }

   public BirdFrightLoopTicker<T> getBirdFrightLoopTicker() {
      return this.birdFrightLoopTicker;
   }

   public BirdFrightTicker<T> getBirdFrightTicker() {
      return this.birdFrightTicker;
   }

   public BirdSentinelTicker<T> getBirdSentinelTicker() {
      return this.birdSentinelTicker;
   }

   public BirdFeatherLoopTicker<T> getBirdFeatherLoopTicker() {
      return this.birdFeatherLoopTicker;
   }

   public void forEachTicker(Consumer<AbstractBirdTicker<T>> consumer) {
      this.tickers.forEach(consumer);
   }

   public DebugLoopTicker<T> getDebugLoopTicker() {
      if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
         BirdCameraMod.LOGGER.warn("[BirdCameraMod] Warn: Trying to get debug loop ticker in production environment! It will not tick.");
      }

      return this.debugLoopTicker;
   }
}
