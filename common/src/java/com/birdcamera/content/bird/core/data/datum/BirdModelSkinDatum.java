package com.birdcamera.content.bird.core.data.datum;

import java.util.List;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;

public record BirdModelSkinDatum(
   List<BirdModel> birdModel,
   List<BirdSkin> birdSkin,
   BirdModelScaleProfile modelScaleProfile,
   float shadowRadius,
   float globalScale,
   float babyScale,
   float maleScale
) {
   public static BirdModelSkinDatum createDefault() {
      return new BirdModelSkinDatum(List.of(BirdModel.createDefault()), List.of(BirdSkin.createDefault()), null, 0.12F, 1.0F, 0.75F, 1.1F);
   }

   public BirdModelSkinDatum withBirdModel(List<BirdModel> birdModel) {
      return new BirdModelSkinDatum(birdModel, this.birdSkin, this.modelScaleProfile, this.shadowRadius, this.globalScale, this.babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withBirdSkin(List<BirdSkin> birdSkin) {
      return new BirdModelSkinDatum(this.birdModel, birdSkin, this.modelScaleProfile, this.shadowRadius, this.globalScale, this.babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withModelScaleProfile(BirdModelScaleProfile modelScaleProfile) {
      return new BirdModelSkinDatum(this.birdModel, this.birdSkin, modelScaleProfile, this.shadowRadius, this.globalScale, this.babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withShadowRadius(float shadowRadius) {
      return new BirdModelSkinDatum(this.birdModel, this.birdSkin, this.modelScaleProfile, shadowRadius, this.globalScale, this.babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withGlobalScale(float globalScale) {
      return new BirdModelSkinDatum(this.birdModel, this.birdSkin, this.modelScaleProfile, this.shadowRadius, globalScale, this.babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withBabyScale(float babyScale) {
      return new BirdModelSkinDatum(this.birdModel, this.birdSkin, this.modelScaleProfile, this.shadowRadius, this.globalScale, babyScale, this.maleScale);
   }

   public BirdModelSkinDatum withMaleScale(float maleScale) {
      return new BirdModelSkinDatum(this.birdModel, this.birdSkin, this.modelScaleProfile, this.shadowRadius, this.globalScale, this.babyScale, maleScale);
   }

   public BirdModel getFirstModel() {
      return this.birdModel.isEmpty() ? null : this.birdModel.getFirst();
   }

   public BirdModel getModel(int index) {
      return index >= 0 && index < this.birdModel.size() ? this.birdModel.get(index) : null;
   }

   public int getModelCount() {
      return this.birdModel.size();
   }

   public BirdSkin getFirstSkin() {
      return this.birdSkin.isEmpty() ? null : this.birdSkin.getFirst();
   }

   public BirdSkin getSkin(int index) {
      return index >= 0 && index < this.birdSkin.size() ? this.birdSkin.get(index) : null;
   }

   public int getSkinCount() {
      return this.birdSkin.size();
   }
}
