package com.birdcamera.content.bird.core.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BirdSkinController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   public int getSkinVariant() {
      BirdData birdData = this.bird.getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdSkin> skins = modelDatum.birdSkin();
      int skinCount = skins.size();
      int variant = (Integer)this.bird.getEntityData().get(AbstractBirdEntity.SKIN_VARIANT);
      return Mth.clamp(variant, 0, skinCount - 1);
   }

   public void setSkinVariant(int variant) {
      BirdData birdData = this.bird.getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdSkin> skins = modelDatum.birdSkin();
      int skinCount = skins.size();
      int clamped = Mth.clamp(variant, 0, skinCount - 1);
      this.bird.getEntityData().set(AbstractBirdEntity.SKIN_VARIANT, clamped);
   }

   public void setSkinVariant(ResourceLocation skinId) {
      List<BirdSkin> skins = this.bird.getBirdData().model().birdSkin();
      int index = -1;

      for (int i = 0; i < skins.size(); i++) {
         if (skins.get(i).id().equals(skinId)) {
            index = i;
            break;
         }
      }

      if (index != -1) {
         this.setSkinVariant(index);
      } else {
         this.setSkinVariant(0);
      }
   }

   public void randomizeSkinVariant() {
      this.setSkinVariant(this.getRandomizeSkinVariant());
   }

   public ResourceLocation getRandomizeSkinVariant() {
      return this.getRandomizeSkinVariant(null, true, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity) {
      return this.getRandomizeSkinVariant(rarity, true, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn) {
      return this.getRandomizeSkinVariant(rarity, natureSpawn, true, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed) {
      return this.getRandomizeSkinVariant(rarity, natureSpawn, breed, true, true, true, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby) {
      return this.getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, true, true, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female) {
      return this.getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, male, female, false, false);
   }

   public ResourceLocation getRandomizeSkinVariant(
      BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique
   ) {
      return this.getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, male, female, unique, false);
   }

   public ResourceLocation getRandomizeSkinVariant(
      BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique, boolean hidden
   ) {
      RandomSource random = this.bird.getRandom();
      BirdData birdData = this.bird.getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdSkin> skins = modelDatum.birdSkin();
      List<BirdSkin> matchingSkins = skins.stream()
         .filter(
            skin -> rarity != null && skin.rarity() != rarity
                  ? false
                  : skin.natureSpawn() == natureSpawn
                     || skin.breed() == breed
                     || skin.baby() == baby
                     || skin.male() == male
                     || skin.female() == female
                     || skin.unique() == unique
                     || skin.hidden() == hidden
         )
         .toList();
      if (matchingSkins.isEmpty()) {
         matchingSkins = Collections.singletonList(skins.getFirst());
      }

      BirdSkin selectedSkin = this.selectSkinByWeight(matchingSkins, random);
      return selectedSkin.id();
   }

   private BirdSkin selectSkinByWeight(List<BirdSkin> skins, RandomSource random) {
      int totalWeight = skins.stream().mapToInt(skinx -> skinx.rarity().getWeight()).sum();
      if (totalWeight == 0) {
         return skins.get(random.nextInt(skins.size()));
      } else {
         int randomValue = random.nextInt(totalWeight);
         int cumulativeWeight = 0;

         for (BirdSkin skin : skins) {
            cumulativeWeight += skin.rarity().getWeight();
            if (randomValue < cumulativeWeight) {
               return skin;
            }
         }

         return skins.getFirst();
      }
   }

   public ResourceLocation inheritSkinVariant(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate, boolean gender) {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdSkin> skins = modelDatum.birdSkin();
      BirdModel model = this.bird().getModel();
      if (skins.size() <= 1) {
         return skins.getFirst().id();
      } else {
         RandomSource random = this.bird().getRandom();
         BirdSkin parentSkin = this.getSkinByIndex(parent.getSkinController().getSkinVariant());
         BirdSkin mateSkin = this.getSkinByIndex(mate.getSkinController().getSkinVariant());
         if (parentSkin != null && mateSkin != null) {
            int targetRarity = this.calcTargetRarity(parentSkin, mateSkin, birdData.misc(), random);
            BirdSkin selected = this.selectSkinByRarity(skins, targetRarity, gender, model);
            return selected != null ? selected.id() : this.fallbackSkin(skins, gender, model);
         } else {
            return this.randomSelectSkin(skins, gender, random, model);
         }
      }
   }

   private ResourceLocation randomSelectSkin(List<BirdSkin> skins, boolean gender, RandomSource random, BirdModel model) {
      List<BirdSkin> filtered = skins.stream().filter(s -> s.breed() && (gender ? s.male() : s.female())).filter(s -> model.supportsSkin(s.id())).toList();
      return filtered.isEmpty() ? this.fallbackSkin(skins, gender, model) : filtered.get(random.nextInt(filtered.size())).id();
   }

   private int calcTargetRarity(BirdSkin parent, BirdSkin mate, BirdMiscDatum misc, RandomSource random) {
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

   private BirdSkin selectSkinByRarity(List<BirdSkin> skins, int target, boolean gender, BirdModel model) {
      target = Math.clamp((long)target, 0, 5);

      for (int offset = 0; offset <= 3; offset++) {
         List<BirdSkin> candidates = new ArrayList<>();

         for (int delta = -offset; delta <= offset; delta += offset == 0 ? 1 : (delta < 0 ? 1 : 2 * offset)) {
            int rarity = target + delta;
            if (rarity >= 0 && rarity <= 5) {
               BirdSkinRarity r = BirdSkinRarity.fromValue(rarity);

               for (BirdSkin skin : skins) {
                  if (skin.rarity() == r && skin.breed() && skin.baby() && (gender ? skin.male() : skin.female()) && model.supportsSkin(skin.id())) {
                     candidates.add(skin);
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

   private ResourceLocation fallbackSkin(List<BirdSkin> skins, boolean gender, BirdModel model) {
      Optional<BirdSkin> skin = skins.stream().filter(s -> s.breed() && (gender ? s.male() : s.female())).filter(s -> model.supportsSkin(s.id())).findFirst();
      if (skin.isPresent()) {
         return skin.get().id();
      } else {
         Optional<BirdSkin> anyBreed = skins.stream().filter(s -> s.breed() && model.supportsSkin(s.id())).findFirst();
         if (anyBreed.isPresent()) {
            return anyBreed.get().id();
         } else {
            Optional<BirdSkin> anyValid = skins.stream().filter(s -> model.supportsSkin(s.id())).findFirst();
            return anyValid.map(BirdSkin::id).orElse(skins.getFirst().id());
         }
      }
   }

   private static float getActualMutantChance(BirdSkin parentSkin, BirdSkin mateSkin, BirdMiscDatum miscDatum) {
      boolean parentIsBreed = !parentSkin.natureSpawn() && parentSkin.breed();
      boolean mateIsBreed = !mateSkin.natureSpawn() && mateSkin.breed();
      float mutantMultiplier = 1.0F;
      if (parentIsBreed && mateIsBreed) {
         mutantMultiplier = miscDatum.mutantP1Boost();
      } else if (parentIsBreed || mateIsBreed) {
         mutantMultiplier = miscDatum.mutantP2Boost();
      }

      float baseMutantChance = miscDatum.mutantChance();
      return baseMutantChance * mutantMultiplier;
   }

   private BirdSkin getSkinByIndex(int index) {
      BirdData birdData = this.bird().getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      List<BirdSkin> skins = modelDatum.birdSkin();
      return index >= 0 && index < skins.size() ? skins.get(index) : null;
   }

   public ResourceLocation textureForVariant(int variant) {
      List<BirdSkin> skins = this.bird.getBirdData().model().birdSkin();
      return variant >= 0 && variant < skins.size() ? skins.get(variant).location() : skins.getFirst().location();
   }
}
