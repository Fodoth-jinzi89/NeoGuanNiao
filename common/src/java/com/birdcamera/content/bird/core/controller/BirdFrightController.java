package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdFrightTicker;
import com.birdcamera.content.bird.core.controller.tick.ticker.BirdPendingFrightTicker;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlyingDatum;
import com.birdcamera.content.bird.core.data.datum.BirdFrightDatum;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BirdFrightController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private Vec3 frightSource;
   private Vec3 pendingFrightSource;

   public void processHurt(@NotNull DamageSource source) {
      BirdBehaviorStateController<T> stateController = this.bird().getBehaviorStateController();
      BirdEatingController<T> eatingController = this.bird().getEatingController();
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      BirdFrightDatum frightDatum = birdData.fright();
      eatingController.clearEating();
      this.bird().getTameController().setInterestedPlayerUUID(null);
      Entity attacker = source.getEntity();
      Vec3 sourcePos = attacker == null ? this.bird().position() : attacker.position();
      boolean isPlayer = attacker instanceof Player;
      if (!this.bird().isTame() || this.bird().getOwner() == null || attacker == null || this.bird().getOwner().getUUID() != attacker.getUUID()) {
         this.setFrightSource(sourcePos);
      }

      this.bird().getNavigation().stop();
      int alertTicks = isPlayer ? miscDatum.alertTicksPlayer() : miscDatum.alertTicksOther();
      stateController.setBehaviorStateFor(BirdBehaviorState.ALERT, alertTicks);
      if (!isPlayer) {
         int delayTicks = frightDatum.frightDelayMin() + this.bird().getRandom().nextInt(frightDatum.frightDelayVariance());
         this.queueFrightFrom(sourcePos, delayTicks);
      }
   }

   public boolean shouldFlee() {
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.bird.getTickController().getTickTimer();
      return !timer.getBirdFrightTicker().isRunning() && !timer.getBirdPendingFrightTicker().isRunning() && this.frightSource != null;
   }

   public void frightenFrom(int ticks) {
      this.frightenFrom(this.frightSource, ticks);
   }

   public void frightenFrom(Vec3 sourcePos, int ticks) {
      BirdTickController<T> tickController = this.bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdBehaviorStateController<T> stateController = this.bird.getBehaviorStateController();
      BirdData birdData = this.bird.getBirdData();
      BirdFrightDatum frightDatum = birdData.fright();
      this.frightSource = sourcePos;
      BirdFrightTicker<? extends AbstractBirdEntity<?>> frightTicker = (BirdFrightTicker<? extends AbstractBirdEntity<?>>)timer.getBirdFrightTicker();
      BirdPendingFrightTicker<? extends AbstractBirdEntity<?>> pendingTicker = (BirdPendingFrightTicker<? extends AbstractBirdEntity<?>>)timer.getBirdPendingFrightTicker();
      int currentTicks = frightTicker.getTicks();
      frightTicker.setTicks(Math.max(currentTicks, ticks));
      pendingTicker.setTicks(0);
      timer.getBirdPendingFrightTicker().pendingFrightDuration = 0;
      this.pendingFrightSource = null;
      int frightLimit = Math.min(frightDatum.frightTicksLimit(), ticks);
      stateController.setBehaviorStateFor(BirdBehaviorState.FLEEING, frightLimit);
      this.alertNearbyBirds();
      if (!this.bird().isBaby() && timer.getBirdFlyingTicker().getTicks() <= 0 && this.bird().onGround()) {
         this.startEscapeFlight(sourcePos);
      }
   }

   protected void startEscapeFlight(Vec3 sourcePos) {
      BirdData birdData = this.bird.getBirdData();
      BirdFlyingDatum flyingDatum = birdData.flying();
      Vec3 away = this.bird.position().subtract(sourcePos);
      if (away.lengthSqr() < 0.01) {
         away = new Vec3(this.bird.getRandom().nextDouble() - 0.5, 0.0, this.bird.getRandom().nextDouble() - 0.5);
      }

      Vec3 direction = new Vec3(away.x, 0.0, away.z).normalize();
      double horizontalDistance = flyingDatum.escapeFlightMinDistance() + this.bird.getRandom().nextDouble() * flyingDatum.escapeFlightDistanceVariance();
      double verticalHeight = flyingDatum.escapeFlightMinHeight() + this.bird.getRandom().nextDouble() * flyingDatum.escapeFlightHeightVariance();
      Vec3 target = this.bird.position().add(direction.scale(horizontalDistance)).add(0.0, verticalHeight, 0.0);
      this.bird.getFlyingController().startShortFlight(target, true);
   }

   public void alertNearbyBirds() {
      BirdData birdData = this.bird.getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      BirdFrightDatum frightDatum = birdData.fright();
      double range = miscDatum.alertNearbyRange();

      for (AbstractBirdEntity<?> b : this.bird.level().getEntitiesOfClass(AbstractBirdEntity.class, this.bird.getBoundingBox().inflate(range))) {
         if (b != this.bird) {
            int alertTicks = miscDatum.alertTicks() + b.getRandom().nextInt(miscDatum.alertTicksVariant());
            b.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, alertTicks);
            BirdTickTimer<? extends AbstractBirdEntity<?>> targetTimer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)b.getTickController().getTickTimer();
            int currentCuriousTicks = targetTimer.getBirdCuriousTicker().getTicks();
            int curiousLimit = miscDatum.curiousTicksLimitForAlert();
            targetTimer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
         }
      }
   }

   public void queueFrightFrom(Vec3 sourcePos, int delayTicks) {
      BirdTickController<T> tickController = this.bird.getTickController();
      BirdTickTimer<? extends AbstractBirdEntity<?>> timer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)tickController.getTickTimer();
      BirdBehaviorStateController<T> stateController = this.bird.getBehaviorStateController();
      BirdEatingController<T> eatingController = this.bird.getEatingController();
      BirdData birdData = this.bird.getBirdData();
      BirdFrightDatum frightDatum = birdData.fright();
      if (eatingController.isEating()) {
         eatingController.clearEating();
      }

      this.pendingFrightSource = sourcePos;
      BirdPendingFrightTicker<? extends AbstractBirdEntity<?>> pendingTicker = (BirdPendingFrightTicker<? extends AbstractBirdEntity<?>>)timer.getBirdPendingFrightTicker();
      int currentPendingDuration = timer.getBirdPendingFrightTicker().pendingFrightDuration;
      int pendingDurationLimit = frightDatum.pendingFrightDurationLimit();
      timer.getBirdPendingFrightTicker().pendingFrightDuration = Math.max(currentPendingDuration, pendingDurationLimit);
      if (pendingTicker.getTicks() <= 0) {
         pendingTicker.setTicks(Math.max(1, delayTicks));
      } else {
         int clampedDelay = Math.clamp((long)delayTicks, 1, pendingTicker.getTicks());
         pendingTicker.setTicks(Math.max(1, clampedDelay));
      }

      int alertLimit = Math.min(frightDatum.pendingFrightTicksLimit(), pendingTicker.getTicks() + 10);
      stateController.setBehaviorStateFor(BirdBehaviorState.ALERT, alertLimit);
      this.alertNearbyBirds();
   }

   public Vec3 getFrightSource() {
      return this.frightSource;
   }

   public void setFrightSource(Vec3 frightSource) {
      this.frightSource = frightSource;
   }

   public Vec3 getPendingFrightSource() {
      return this.pendingFrightSource;
   }

   public void setPendingFrightSource(Vec3 pendingFrightSource) {
      this.pendingFrightSource = pendingFrightSource;
   }
}
