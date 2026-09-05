package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import net.fabricmc.loader.api.FabricLoader;

public abstract class AbstractBirdTicker<T extends AbstractBirdEntity<T>> {
   private int ticks;
   private T bird;
   protected final boolean shouldTickCommon;
   protected final boolean shouldTickClient;
   protected final boolean isLoopTicker;
   private boolean frozen = false;

   public AbstractBirdTicker() {
      this(true, true);
   }

   public AbstractBirdTicker(boolean shouldTickCommon, boolean shouldTickClient) {
      this(shouldTickCommon, shouldTickClient, false);
   }

   public AbstractBirdTicker(boolean shouldTickCommon, boolean shouldTickClient, boolean isLoopTicker) {
      this.shouldTickCommon = shouldTickCommon;
      this.shouldTickClient = shouldTickClient;
      this.isLoopTicker = isLoopTicker;
   }

   public final void attach(T bird) {
      if (this.bird != null) {
         throw new IllegalStateException("Ticker is already attached");
      } else {
         this.bird = bird;
         this.onAttach();
      }
   }

   protected void onAttach() {
   }

   protected final T bird() {
      if (this.bird == null) {
         throw new IllegalStateException("Ticker is not attached");
      } else {
         return this.bird;
      }
   }

   public int getTicks() {
      return this.ticks;
   }

   public void setTicks(int ticks) {
      if (ticks < 0) {
         throw new IllegalArgumentException("Ticks cannot be negative: " + ticks);
      } else {
         this.onSet(this.ticks, ticks);
         this.ticks = ticks;
      }
   }

   public void setTicksWithVariance(int ticks, int variance) {
      this.setTicks(ticks + this.bird().getRandom().nextInt(variance));
   }

   public boolean shouldTickClient() {
      return this.shouldTickClient;
   }

   public boolean shouldTickCommon() {
      return this.shouldTickCommon;
   }

   public boolean isLoopTicker() {
      return this.isLoopTicker;
   }

   public void tick() {
      if (this.shouldTickCommon && !this.isFrozen()) {
         if (this.isLoopTicker && this.ticks <= 0) {
            this.reset();
            this.updateFrozen();
            if (this.isFrozen()) {
               return;
            }
         }

         if (this.ticks > 0) {
            this.ticks--;
            this.run();
            if (this.ticks <= 0) {
               this.onExpire();
            }
         }
      }
   }

   public void tickClient() {
      if (this.shouldTickClient && !this.isFrozen()) {
         if (this.isLoopTicker && this.ticks <= 0) {
            this.resetClient();
            this.updateFrozenClient();
            if (this.isFrozen()) {
               return;
            }
         }

         if (this.ticks > 0) {
            this.ticks--;
            this.runClient();
            if (this.ticks <= 0) {
               this.onExpireClient();
            }
         }
      }
   }

   protected void run() {
   }

   protected void runClient() {
      this.run();
   }

   protected void reset() {
      this.onReset();
   }

   protected void resetClient() {
      this.reset();
   }

   protected void onExpire() {
      if (this.enableLifecycleLog() && !this.isLoopTicker) {
         BirdCameraMod.LOGGER.info("[Ticker] {} expired", this.debugName());
      }
   }

   protected void onExpireClient() {
      this.onExpire();
   }

   protected void onSet(int ticksOld, int ticksNew) {
      if (this.enableLifecycleLog() && !this.isLoopTicker) {
         BirdCameraMod.LOGGER.info("[Ticker] {}: Set {} -> {}", new Object[]{this.debugName(), ticksOld, ticksNew});
      }
   }

   protected void onReset() {
      if (this.enableLifecycleLog()) {
         BirdCameraMod.LOGGER.info("[LoopTicker] {} reset ({})", this.debugName(), this.ticks);
      }
   }

   public final boolean isRunning() {
      return this.ticks > 0;
   }

   public final void onDebug() {
      this.debug();
   }

   protected void debug() {
   }

   protected String debugName() {
      String name = this.getClass().getSimpleName();
      if (name.startsWith("Bird")) {
         name = name.substring(4);
      }

      if (name.endsWith("Ticker")) {
         name = name.substring(0, name.length() - 6);
      }

      return name;
   }

   public final String debugLine() {
      String state = this.isRunning() ? "✔" : "✘";
      if (this.isFrozen()) {
         state = "□";
      }

      return String.format(" %-28s %s %4d", this.debugName(), state, this.ticks);
   }

   public final boolean enableLifecycleLog() {
      return FabricLoader.getInstance().isDevelopmentEnvironment();
   }

   public boolean isFrozen() {
      return this.frozen;
   }

   public void setFrozen(boolean frozen) {
      this.frozen = frozen;
   }

   protected void updateFrozen() {
   }

   protected void updateFrozenClient() {
      this.updateFrozen();
   }
}
