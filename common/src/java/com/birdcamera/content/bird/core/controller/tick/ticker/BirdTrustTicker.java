package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import net.minecraft.util.Mth;

public class BirdTrustTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public BirdTrustTicker() {
      super(true, false);
   }

   public void addTrust(int amount) {
      BirdData birdData = this.bird().getBirdData();
      int trustLimit = birdData.tame().trustTicksLimit();
      this.setTicks(Mth.clamp(this.getTicks() + amount, 0, trustLimit));
   }
}
