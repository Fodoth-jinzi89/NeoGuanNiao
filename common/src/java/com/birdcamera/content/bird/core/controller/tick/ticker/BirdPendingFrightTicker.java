package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdFrightController;
import com.birdcamera.content.bird.core.data.BirdData;
import net.minecraft.world.phys.Vec3;

public class BirdPendingFrightTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public int pendingFrightDuration;

   public BirdPendingFrightTicker() {
      super(true, false);
   }

   @Override
   protected void run() {
      BirdFrightController<T> frightController = this.bird().getFrightController();
      BirdData birdData = this.bird().getBirdData();
      this.bird().getNavigation().stop();
      Vec3 sourcePos = frightController.getPendingFrightSource();
      if (sourcePos != null) {
         double lookX = sourcePos.x;
         double lookY = sourcePos.y + birdData.fright().pendingFrightLookYOffset();
         double lookZ = sourcePos.z;
         float lookSpeed = birdData.fright().pendingFrightLookSpeed();
         this.bird().getLookControl().setLookAt(lookX, lookY, lookZ, lookSpeed, lookSpeed);
      }
   }

   @Override
   protected void onExpire() {
      super.onExpire();
      BirdFrightController<T> frightController = this.bird().getFrightController();
      BirdData birdData = this.bird().getBirdData();
      Vec3 sourcePos = frightController.getPendingFrightSource() != null ? frightController.getPendingFrightSource() : this.bird().position();
      int minDuration = birdData.fright().pendingFrightMinDuration();
      int duration = Math.max(minDuration, this.pendingFrightDuration);
      frightController.frightenFrom(sourcePos, duration);
   }
}
