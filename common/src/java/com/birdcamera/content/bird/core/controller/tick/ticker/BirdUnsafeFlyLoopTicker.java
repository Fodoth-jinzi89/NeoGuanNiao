package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlyingDatum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BirdUnsafeFlyLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdUnsafeFlyLoopTicker() {
      super(true, false, true);
   }

   @Override
   protected void reset() {
      super.reset();
      this.setTicks(20);
      this.processUnsafeFlying();
   }

   private void processUnsafeFlying() {
      T bird = this.bird();
      boolean flag1 = !bird.onGround();
      boolean flag2 = !bird.getFlyingController().isBirdFlightActive();
      boolean flag3 = !bird.isPassenger();
      boolean flag4 = bird.getTickController().getTickTimer().getBirdLandingTicker().getTicks() == 0;
      boolean flag5 = bird.getBehaviorStateController().getBehaviorState().isUnsafeFlyTickerEnabled();
      boolean flag6 = bird.getGoalController().getBirdBathUseGoalController().isRunning();
      boolean flag7 = bird.isBaby();
      boolean flag8 = bird.isTame();
      boolean flag9 = bird.isLeashed();
      boolean isUnsafeFlying = flag1 && flag2 && flag3 && flag4 && flag5 && flag6 && flag7 && flag8 && flag9;
      if (!isUnsafeFlying) {
         if (this.enableLifecycleLog() && (double)this.bird().getRandom().nextFloat() <= 0.1) {
            BirdCameraMod.LOGGER
               .info(
                  "[Ticker] UnsafeFly: Early Return with flags: {} {} {} {} {} {} {} {} {}",
                  new Object[]{flag1, flag2, flag3, flag4, flag5, flag6, flag7, flag8, flag9}
               );
         }
      } else {
         boolean roosting = this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING;
         boolean stuckInAir = this.bird().getDeltaMovement().length() < 0.2;
         if (roosting) {
            if (!stuckInAir && this.enableLifecycleLog()) {
               BirdCameraMod.LOGGER
                  .info("[Ticker] UnsafeFly: Bird roosting properly with movement length: {}, skip check", this.bird().getDeltaMovement().length());
            }
         } else {
            boolean sleeping = this.bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SLEEPING;
            boolean isSleepingPlaceStillValid = this.positionStillValid();
            if (sleeping && isSleepingPlaceStillValid) {
               if (this.enableLifecycleLog() && this.bird().getTickController().getTickTimer().getDebugLoopTicker().getTicks() < 5) {
                  BirdCameraMod.LOGGER.info("[Ticker] UnsafeFly: Bird sleeping in valid place, raise alertness and skip check");
               }

               this.setTicks(Math.min(this.getTicks(), 5));
            } else {
               BirdData birdData = bird.getBirdData();
               BirdFlyingDatum flyingDatum = birdData.flying();
               int dataTicks = flyingDatum.ambientAirCruiseMinTicks() + bird.getRandom().nextInt(flyingDatum.ambientAirCruiseRandomTicks());
               int landingTicks = this.getTicks() + dataTicks;
               bird.getFlyingController().startShortFlight(null, false);
               BirdCameraMod.LOGGER.info("[Ticker] UnsafeFly: Start with Data Ticks: {}, LandingTicks: {}", dataTicks, landingTicks);
               this.setTicks(Math.min((int)((double)landingTicks * 2.5), dataTicks * 10));
            }
         }
      }
   }

   @Override
   protected void onReset() {
   }

   public boolean positionStillValid() {
      BlockPos pos = this.bird().blockPosition();
      BlockPos belowPos = pos.below();
      BlockState currentState = this.bird().level().getBlockState(pos);
      BlockState belowState = this.bird().level().getBlockState(belowPos);
      boolean currentIsAir = currentState.isAir() || currentState.is(Blocks.AIR);
      boolean belowIsAir = belowState.isAir() || belowState.is(Blocks.AIR);
      boolean belowIsLeaves = belowState.getBlock() instanceof LeavesBlock;
      if (!(currentState.getBlock() instanceof LeavesBlock)) {
         return currentIsAir ? !belowIsAir : false;
      } else {
         return belowIsAir || belowIsLeaves;
      }
   }
}
