package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import net.minecraft.util.Mth;

public class BirdFeatherController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public void setFeatherCount(int featherCount) {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      this.bird()
         .getEntityData()
         .set(
            AbstractBirdEntity.FEATHER_COUNT,
            Mth.clamp(featherCount, miscDatum.featherCountMin(), miscDatum.featherCountMin() + miscDatum.featherCountVariance())
         );
   }

   public void setFeatherInterval(int featherInterval) {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      this.bird()
         .getEntityData()
         .set(
            AbstractBirdEntity.FEATHER_INTERVAL,
            Mth.clamp(
               featherInterval,
               miscDatum.featherIntervalMiddle() - miscDatum.featherIntervalVariance() / 2,
               miscDatum.featherIntervalMiddle() + miscDatum.featherIntervalVariance() / 2
            )
         );
   }

   public int getFeatherCount() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int featherCount = (Integer)this.bird().getEntityData().get(AbstractBirdEntity.FEATHER_COUNT);
      return Mth.clamp(featherCount, miscDatum.featherCountMin(), miscDatum.featherCountMin() + miscDatum.featherCountVariance());
   }

   public int getFeatherInterval() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int featherInterval = (Integer)this.bird().getEntityData().get(AbstractBirdEntity.FEATHER_INTERVAL);
      return Mth.clamp(
         featherInterval,
         miscDatum.featherIntervalMiddle() - miscDatum.featherIntervalVariance() / 2,
         miscDatum.featherIntervalMiddle() + miscDatum.featherIntervalVariance() / 2
      );
   }

   public void randomizeFeatherCount() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int min = miscDatum.featherCountMin();
      int variance = miscDatum.featherCountVariance();
      this.setFeatherCount(min + this.bird().getRandom().nextInt(variance + 1));
   }

   public void randomizeFeatherInterval() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int middle = miscDatum.featherIntervalMiddle();
      int variance = miscDatum.featherIntervalVariance();
      int min = middle - variance / 2;
      int max = middle + variance / 2;
      this.setFeatherInterval(min + this.bird().getRandom().nextInt(max - min + 1));
   }

   public int getRandomFeatherCount() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int min = miscDatum.featherCountMin();
      int variance = miscDatum.featherCountVariance();
      return min + this.bird().getRandom().nextInt(variance + 1);
   }

   public int getRandomFeatherInterval() {
      BirdData birdData = this.bird().getBirdData();
      BirdMiscDatum miscDatum = birdData.misc();
      int middle = miscDatum.featherIntervalMiddle();
      int variance = miscDatum.featherIntervalVariance();
      int min = middle - variance / 2;
      int max = middle + variance / 2;
      return min + this.bird().getRandom().nextInt(max - min + 1);
   }

   public int inheritFeatherCount(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
      int parentFeatherCount = parent.getFeatherCount();
      int mateFeatherCount = mate.getFeatherCount();
      BirdMiscDatum miscDatum = parent.getBirdData().misc();
      BirdMiscDatum misc = parent.getBirdData().misc();
      BirdMiscDatum misc1 = mate.getBirdData().misc();
      int globalMin = Math.min(misc.featherCountMin(), misc1.featherCountMin());
      int globalMax = Math.min(misc.featherCountMin() + misc.featherCountVariance(), misc1.featherCountMin() + misc1.featherCountVariance());
      double mean = (double)(parentFeatherCount + mateFeatherCount) / 2.0;
      int range = Math.max(1, (int)((double)(globalMax - globalMin) * 0.3));
      double randomOffset = (parent.getRandom().nextDouble() * 2.0 - 1.0) * (double)range;
      double skewFactor = parent.getRandom().nextDouble();
      double skewed = skewFactor * skewFactor;
      double resultDouble = mean + randomOffset * (0.5 + 0.5 * skewed);
      int result = (int)Math.round(resultDouble);
      return Math.clamp((long)result, globalMin, globalMax);
   }

   public int inheritFeatherInterval(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
      int parentFeatherInterval = parent.getFeatherInterval();
      int mateFeatherInterval = mate.getFeatherInterval();
      BirdMiscDatum misc = parent.getBirdData().misc();
      BirdMiscDatum misc1 = mate.getBirdData().misc();
      int globalMin = Math.min(
         misc.featherIntervalMiddle() - misc.featherIntervalVariance() / 2, misc1.featherIntervalMiddle() - misc1.featherIntervalVariance() / 2
      );
      int globalMax = Math.min(
         misc.featherIntervalMiddle() + misc.featherIntervalVariance() / 2, misc1.featherIntervalMiddle() + misc1.featherIntervalVariance() / 2
      );
      double mean = (double)(parentFeatherInterval + mateFeatherInterval) / 2.0;
      int range = Math.max(1, (int)((double)(globalMax - globalMin) * 0.3));
      double randomOffset = (parent.getRandom().nextDouble() * 2.0 - 1.0) * (double)range;
      double skewFactor = parent.getRandom().nextDouble();
      double skewed = 1.0 - skewFactor * skewFactor;
      double resultDouble = mean + randomOffset * (0.5 + 0.5 * skewed);
      int result = (int)Math.round(resultDouble);
      return Math.clamp((long)result, globalMin, globalMax);
   }
}
