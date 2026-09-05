package com.birdcamera.content.bird.core.goal;

import java.util.EnumSet;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdGoalController;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.controller.tick.ticker.DebugLoopTicker;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.fabricmc.loader.api.FabricLoader;

public abstract class AbstractBirdGoal extends Goal {
   private final AbstractBirdEntity<?> bird;
   private int repathTicks;
   private final int maxRepathTicks;

   public AbstractBirdGoal(AbstractBirdEntity<?> bird) {
      this(bird, 10);
   }

   public AbstractBirdGoal(AbstractBirdEntity<?> bird, int maxRepathTicks) {
      this.bird = bird;
      this.maxRepathTicks = maxRepathTicks;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public final boolean canUse() {
      return this.defaultUsePredicates() && this.usePredicates();
   }

   public final boolean canContinueToUse() {
      return this.defaultContinuePredicates() && this.continuePredicates();
   }

   public final void start() {
      this.repathTicks = 0;
      this.debugStart();
      this.onStart();
   }

   protected void debugStart() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         DebugLoopTicker<? extends AbstractBirdEntity<?>> debugTicker = (DebugLoopTicker<? extends AbstractBirdEntity<?>>)this.bird()
            .getTickController()
            .getTickTimer()
            .getDebugLoopTicker();
         if (debugTicker.enableLifecycleLog()) {
            debugTicker.debugGoalStart(this.getClass().getSimpleName());
         }
      }
   }

   public final void tick() {
      if (this.shouldTick()) {
         this.onTick();
         if (--this.repathTicks <= 0) {
            this.debugReset();
            this.reset();
            this.onReset();
         }
      }
   }

   protected boolean shouldTick() {
      return this.individualGoalController().shouldTick();
   }

   protected void reset() {
      this.repathTicks = this.maxRepathTicks;
   }

   public final void stop() {
      this.repathTicks = 0;
      this.debugStop();
      this.onStop();
   }

   protected void debugStop() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         DebugLoopTicker<? extends AbstractBirdEntity<?>> debugTicker = (DebugLoopTicker<? extends AbstractBirdEntity<?>>)this.bird()
            .getTickController()
            .getTickTimer()
            .getDebugLoopTicker();
         if (debugTicker.enableLifecycleLog()) {
            debugTicker.debugGoalStop(this.getClass().getSimpleName());
         }
      }
   }

   protected void debugReset() {
   }

   protected void onStart() {
      this.individualGoalController().onStart();
   }

   protected void onTick() {
      this.individualGoalController().onTick();
   }

   protected void onReset() {
      this.individualGoalController().onReset();
   }

   protected void onStop() {
      this.individualGoalController().onStop();
   }

   protected boolean defaultUsePredicates() {
      return !this.bird().isRemoved()
         && !this.bird().isDeadOrDying()
         && !this.bird().isNoAi()
         && !this.bird().isPassenger()
         && !this.bird().isFullyFrozen()
         && !this.bird().isLeashed()
         && !this.bird().isRemoved()
         && this.bird().getRandom().nextInt(this.individualGoalController().chance()) == 0;
   }

   protected boolean defaultContinuePredicates() {
      return !this.bird().getEatingController().isEating()
         && !this.bird().isDancing()
         && !this.bird().getRoutineController().isSleepingOrRoosting()
         && !this.bird().getBehaviorStateController().getBehaviorState().isEscape();
   }

   protected boolean usePredicates() {
      return this.individualGoalController().canUse() ? this.individualGoalController().onUse() : false;
   }

   protected boolean continuePredicates() {
      return this.individualGoalController().canContinue() ? this.individualGoalController().onContinue() : false;
   }

   public AbstractBirdEntity<?> bird() {
      return this.bird;
   }

   public int getMaxRepathTicks() {
      return this.maxRepathTicks;
   }

   protected BirdGoalDatum goalDatum() {
      return this.bird().getBirdData().goal();
   }

   protected BirdGoalController<?> goalController() {
      return this.bird().getGoalController();
   }

   protected AbstractGoalController<?> individualGoalController() {
      return null;
   }

   protected int getRepathTicks() {
      return this.repathTicks;
   }

   protected void setRepathTicks(int repathTicks) {
      this.repathTicks = repathTicks;
   }

   protected void setRepathTicksWithVariance(int repathTicks, int variance) {
      this.setRepathTicks(repathTicks + this.bird().getRandom().nextInt(variance));
   }
}
