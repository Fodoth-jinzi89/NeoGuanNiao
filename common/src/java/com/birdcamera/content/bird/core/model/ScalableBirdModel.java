package com.birdcamera.content.bird.core.model;

import com.birdcamera.content.bird.core.data.datum.BirdModelScaleProfile;
import net.minecraft.util.RandomSource;

public interface ScalableBirdModel {
   BirdModelScaleProfile modelScaleProfile();

   float getIndividualModelScale();

   void setIndividualModelScale(float var1);

   default float getModelRenderScale() {
      return BirdModelScale.renderScale(this.modelScaleProfile(), this.getIndividualModelScale());
   }

   default boolean isIndividualScaleValid() {
      return this.modelScaleProfile().isScaleValid(this.getIndividualModelScale());
   }

   default void randomizeIndividualScale(RandomSource random) {
      this.setIndividualModelScale(BirdModelScale.randomIndividualScale(random, this.modelScaleProfile()));
   }

   default void inheritIndividualScale(RandomSource random, ScalableBirdModel firstParent, ScalableBirdModel secondParent) {
      float childScale = BirdModelScale.inheritIndividualScale(
         random, firstParent.getIndividualModelScale(), secondParent.getIndividualModelScale(), this.modelScaleProfile()
      );
      this.setIndividualModelScale(childScale);
   }

   default float getMidIndividualScale() {
      return this.modelScaleProfile().midIndividualScale();
   }

   default float getIndividualScaleRange() {
      return this.modelScaleProfile().individualScaleRange();
   }
}
