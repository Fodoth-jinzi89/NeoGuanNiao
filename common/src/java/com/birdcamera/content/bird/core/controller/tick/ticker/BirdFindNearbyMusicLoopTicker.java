package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import net.minecraft.core.BlockPos;

public class BirdFindNearbyMusicLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdFindNearbyMusicLoopTicker() {
      super(true, false, true);
   }

   @Override
   protected void updateFrozen() {
      boolean shouldFreeze = this.bird().getRoutineController().isRoostTime();
      this.setFrozen(shouldFreeze);
   }

   @Override
   protected void reset() {
      super.reset();
      if (this.bird() instanceof BudgerigarEntity budgerigar) {
         this.setTicks(18 + budgerigar.getRandom().nextInt(14));
         BlockPos sourcePos = budgerigar.findNearbyJukebox();
         if (sourcePos != null) {
            budgerigar.triggerMusic(85 + budgerigar.getRandom().nextInt(35));

            for (BudgerigarEntity budgerigar1 : budgerigar.level().getEntitiesOfClass(BudgerigarEntity.class, budgerigar.getBoundingBox().inflate(10.0))) {
               if (budgerigar1 != budgerigar && budgerigar1.getRandom().nextFloat() < 0.8F) {
                  budgerigar1.triggerMusic(65 + budgerigar1.getRandom().nextInt(35));
               }
            }
         }
      }
   }

   @Override
   protected void onReset() {
   }
}
