package com.birdcamera.content.bird.core.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdModelScaleProfile;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.model.BirdModelRarity;
import com.birdcamera.content.bird.core.model.BirdModelScale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BirdModelController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public void randomizeModelScale() {
      RandomSource random = this.bird().getRandom();
      BirdModelScaleProfile profile = this.bird().modelScaleProfile();
      float scale = BirdModelScale.randomIndividualScale(random, profile);
      this.setIndividualModelScale(scale);
   }

   public void setIndividualModelScale(float scale) {
      BirdModelScaleProfile profile = this.bird().modelScaleProfile();
      float sanitizedScale = BirdModelScale.sanitize(scale, profile);
      this.bird().getEntityData().set(AbstractBirdEntity.MODEL_SCALE, sanitizedScale);
   }

   public BirdModelScaleProfile modelScaleProfile() {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      return modelDatum.modelScaleProfile();
   }

   public float getIndividualModelScale() {
      BirdModelScaleProfile profile = this.modelScaleProfile();
      float scale = (Float)this.bird().getEntityData().get(AbstractBirdEntity.MODEL_SCALE);
      return BirdModelScale.sanitize(scale, profile);
   }

   public float getRenderModelScale() {
      BirdModelScaleProfile profile = this.modelScaleProfile();
      float scale = (Float)this.bird().getEntityData().get(AbstractBirdEntity.MODEL_SCALE);
      return BirdModelScale.renderScale(profile, scale);
   }

   public int getModelVariant() {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdModel> models = modelDatum.birdModel();
      int modelCount = models.size();
      int variant = (Integer)this.bird().getEntityData().get(AbstractBirdEntity.MODEL_VARIANT);
      return Mth.clamp(variant, 0, modelCount - 1);
   }

   public void setModelVariant(int variant) {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdModel> models = modelDatum.birdModel();
      int modelCount = models.size();
      int clamped = Mth.clamp(variant, 0, modelCount - 1);
      this.bird().getEntityData().set(AbstractBirdEntity.MODEL_VARIANT, clamped);
   }

   public void setModelVariant(ResourceLocation modelId) {
      List<BirdModel> models = this.bird().getBirdData().model().birdModel();
      int index = -1;

      for (int i = 0; i < models.size(); i++) {
         if (models.get(i).id().equals(modelId)) {
            index = i;
            break;
         }
      }

      if (index != -1) {
         this.setModelVariant(index);
      } else {
         this.setModelVariant(0);
      }
   }

   public void randomizeModelVariant() {
      this.setModelVariant(this.getRandomizeModelVariant());
   }

   public ResourceLocation getRandomizeModelVariant() {
      return this.getRandomizeModelVariant(null, true, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity) {
      return this.getRandomizeModelVariant(rarity, true, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn) {
      return this.getRandomizeModelVariant(rarity, natureSpawn, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed) {
      return this.getRandomizeModelVariant(rarity, natureSpawn, breed, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby) {
      return this.getRandomizeModelVariant(rarity, natureSpawn, breed, baby, true, true, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female) {
      return this.getRandomizeModelVariant(rarity, natureSpawn, breed, baby, male, female, false, false);
   }

   public ResourceLocation getRandomizeModelVariant(
      BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique
   ) {
      return this.getRandomizeModelVariant(rarity, natureSpawn, breed, baby, male, female, unique, false);
   }

   public ResourceLocation getRandomizeModelVariant(
      BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique, boolean hidden
   ) {
      RandomSource random = this.bird().getRandom();
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdModel> models = modelDatum.birdModel();
      List<BirdModel> matchingModels = models.stream()
         .filter(
            model -> rarity != null && model.rarity() != rarity
                  ? false
                  : model.natureSpawn() == natureSpawn
                     || model.breed() == breed
                     || model.baby() == baby
                     || model.male() == male
                     || model.female() == female
                     || model.unique() == unique
                     || model.hidden() == hidden
         )
         .toList();
      if (matchingModels.isEmpty()) {
         matchingModels = Collections.singletonList(models.getFirst());
      }

      BirdModel selectedModel = this.selectModelByWeight(matchingModels, random);
      return selectedModel.id();
   }

   private BirdModel selectModelByWeight(List<BirdModel> models, RandomSource random) {
      int totalWeight = models.stream().mapToInt(modelx -> modelx.rarity().getWeight()).sum();
      if (totalWeight == 0) {
         return models.get(random.nextInt(models.size()));
      } else {
         int randomValue = random.nextInt(totalWeight);
         int cumulativeWeight = 0;

         for (BirdModel model : models) {
            cumulativeWeight += model.rarity().getWeight();
            if (randomValue < cumulativeWeight) {
               return model;
            }
         }

         return models.getFirst();
      }
   }

   public ResourceLocation inheritModelVariant(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate, boolean gender) {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdModel> models = modelDatum.birdModel();
      if (models.size() <= 1) {
         return models.getFirst().id();
      } else {
         RandomSource random = this.bird().getRandom();
         BirdModel parentModel = this.getModelByIndex(parent.getModelController().getModelVariant());
         BirdModel mateModel = this.getModelByIndex(mate.getModelController().getModelVariant());
         if (parentModel != null && mateModel != null) {
            int targetRarity = this.calcTargetRarity(parentModel, mateModel, birdData.misc(), random);
            BirdModel selected = this.selectModelByRarity(models, targetRarity, gender);
            return selected != null ? selected.id() : this.fallbackModel(models, gender);
         } else {
            return this.randomSelectModel(models, gender, random);
         }
      }
   }

   private ResourceLocation randomSelectModel(List<BirdModel> models, boolean gender, RandomSource random) {
      List<BirdModel> filtered = models.stream().filter(m -> m.breed() && (gender ? m.male() : m.female())).toList();
      return filtered.isEmpty() ? models.getFirst().id() : filtered.get(random.nextInt(filtered.size())).id();
   }

   private int calcTargetRarity(BirdModel parent, BirdModel mate, BirdMiscDatum misc, RandomSource random) {
      int pRarity = parent.rarity().getRarity();
      int mRarity = mate.rarity().getRarity();
      double baseMean = (double)(pRarity + mRarity) / 2.0;
      boolean isMutant = random.nextFloat() < getActualMutantChance(parent, mate, misc);
      if (isMutant) {
         double offset = random.nextDouble();
         double mean = offset < (double)misc.mutantL1Cap() ? baseMean : (offset < (double)misc.mutantL2Cap() ? baseMean + 1.0 : baseMean + 2.0);
         mean = Math.min(mean, 5.0);
         double stdDev = Math.max((double)Math.abs(pRarity - mRarity) / 4.0 + 0.3, 0.3);
         return (int)Math.round(mean + this.gaussianZ(random) * stdDev);
      } else {
         double stdDev = Math.max((double)Math.abs(pRarity - mRarity) / 2.0 + 0.5, 0.5);
         return (int)Math.round(baseMean + this.gaussianZ(random) * stdDev);
      }
   }

   private double gaussianZ(RandomSource random) {
      double u1 = random.nextDouble();
      double u2 = random.nextDouble();
      return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos((Math.PI * 2) * u2);
   }

   private BirdModel selectModelByRarity(List<BirdModel> models, int target, boolean gender) {
      target = Math.clamp((long)target, 0, 5);

      for (int offset = 0; offset <= 3; offset++) {
         List<BirdModel> candidates = new ArrayList<>();

         for (int delta = -offset; delta <= offset; delta += offset == 0 ? 1 : (delta < 0 ? 1 : 2 * offset)) {
            int rarity = target + delta;
            if (rarity >= 0 && rarity <= 5) {
               BirdModelRarity r = BirdModelRarity.fromValue(rarity);

               for (BirdModel model : models) {
                  if (model.rarity() == r && model.breed() && model.baby() && (gender ? model.male() : model.female())) {
                     candidates.add(model);
                  }
               }
            }
         }

         if (!candidates.isEmpty()) {
            return candidates.get(this.bird().getRandom().nextInt(candidates.size()));
         }
      }

      return null;
   }

   private ResourceLocation fallbackModel(List<BirdModel> models, boolean gender) {
      Optional<BirdModel> model = models.stream().filter(m -> m.breed() && (gender ? m.male() : m.female())).findFirst();
      return model.map(BirdModel::id).orElse(models.getFirst().id());
   }

   private static float getActualMutantChance(BirdModel parentModel, BirdModel mateModel, BirdMiscDatum miscDatum) {
      boolean parentIsBreed = !parentModel.natureSpawn() && parentModel.breed();
      boolean mateIsBreed = !mateModel.natureSpawn() && mateModel.breed();
      float mutantMultiplier = 1.0F;
      if (parentIsBreed && mateIsBreed) {
         mutantMultiplier = miscDatum.mutantP1Boost();
      } else if (parentIsBreed || mateIsBreed) {
         mutantMultiplier = miscDatum.mutantP2Boost();
      }

      float baseMutantChance = miscDatum.mutantChance();
      return baseMutantChance * mutantMultiplier;
   }

   private BirdModel getModelByIndex(int index) {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdModel> models = modelDatum.birdModel();
      return index >= 0 && index < models.size() ? models.get(index) : null;
   }

   private void setModelVariantByModel(BirdModel model, List<BirdModel> allModels) {
      for (int i = 0; i < allModels.size(); i++) {
         if (allModels.get(i).id().equals(model.id())) {
            this.setModelVariant(i);
            return;
         }
      }

      this.setModelVariant(0);
   }

   public ResourceLocation modelForVariant(int variant) {
      List<BirdModel> models = this.bird().getBirdData().model().birdModel();
      return variant >= 0 && variant < models.size() ? models.get(variant).location() : models.getFirst().location();
   }

   public ResourceLocation modelForVariantId(int variant) {
      List<BirdModel> models = this.bird().getBirdData().model().birdModel();
      return variant >= 0 && variant < models.size() ? models.get(variant).id() : models.getFirst().id();
   }
}
