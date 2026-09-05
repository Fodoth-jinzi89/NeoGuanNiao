package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.model.BirdModel;

public class BirdModelValidateGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Override
   public int chance() {
      return 20;
   }

   @Override
   public boolean canUse() {
      boolean valid = this.isModelValid();
      return !valid;
   }

   private boolean isModelValid() {
      BirdModel model = this.bird().getModel();
      String family = this.getModelFamily(model.id().getPath());
      if (this.hasBabyVariant(family)) {
         if (this.bird().isBaby() && !model.baby()) {
            return false;
         }

         if (!this.bird().isBaby() && model.baby()) {
            return false;
         }
      }

      if (this.hasGenderVariant(family)) {
         return this.bird().isMale() && !model.male() ? false : this.bird().isMale() || model.female();
      } else {
         return true;
      }
   }

   @Override
   public boolean canContinue() {
      return false;
   }

   @Override
   public void onStop() {
      this.validateModel();
   }

   private void validateModel() {
      BirdModel current = this.bird().getModel();
      BirdModel target = this.findReplacement(current);
      if (!target.id().equals(current.id())) {
         this.bird().getModelController().setModelVariant(target.id());
      }
   }

   private BirdModel findReplacement(BirdModel current) {
      String family = this.getModelFamily(current.id().getPath());
      BirdModel best = null;

      for (BirdModel model : this.bird().getBirdData().model().birdModel()) {
         if (this.getModelFamily(model.id().getPath()).equals(family) && this.isModelAvailable(model)) {
            best = model;
            if (model.id().equals(current.id())) {
               return model;
            }
         }
      }

      return best == null ? current : best;
   }

   private boolean isModelAvailable(BirdModel model) {
      if (this.bird().isBaby() && !model.baby()) {
         return false;
      } else if (!this.bird().isBaby() && model.baby()) {
         return false;
      } else {
         return this.bird().isMale() && !model.male() ? false : this.bird().isMale() || model.female();
      }
   }

   private String getModelFamily(String id) {
      String[] parts = id.split("_");
      StringBuilder family = new StringBuilder();

      for (String part : parts) {
         if (!part.equals("baby") && !part.equals("male") && !part.equals("female")) {
            if (!family.isEmpty()) {
               family.append("_");
            }

            family.append(part);
         }
      }

      return family.toString();
   }

   private boolean hasBabyVariant(String family) {
      for (BirdModel model : this.bird().getBirdData().model().birdModel()) {
         String modelFamily = this.getModelFamily(model.id().getPath());
         if (modelFamily.equals(family) && model.baby()) {
            return true;
         }
      }

      return false;
   }

   private boolean hasGenderVariant(String family) {
      boolean male = false;
      boolean female = false;

      for (BirdModel model : this.bird().getBirdData().model().birdModel()) {
         String modelFamily = this.getModelFamily(model.id().getPath());
         if (modelFamily.equals(family)) {
            if (model.male()) {
               male = true;
            }

            if (model.female()) {
               female = true;
            }

            if (male && female) {
               return true;
            }
         }
      }

      return false;
   }
}
