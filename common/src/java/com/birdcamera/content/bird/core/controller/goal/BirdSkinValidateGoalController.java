package com.birdcamera.content.bird.core.controller.goal;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.content.bird.core.skin.BirdSkinUtils;

public class BirdSkinValidateGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {
   @Override
   public int chance() {
      return 20;
   }

   @Override
   public boolean canUse() {
      return !this.isSkinValid();
   }

   private boolean isSkinValid() {
      BirdSkin skin = this.bird().getSkin();
      BirdModel model = this.bird().getModel();
      return !model.supportsSkin(skin.id()) ? false : BirdSkinUtils.isSkinAvailable(this.bird(), skin);
   }

   @Override
   public boolean canContinue() {
      return false;
   }

   @Override
   public void onStop() {
      this.validateSkin();
   }

   private void validateSkin() {
      BirdSkin current = this.bird().getSkin();
      BirdSkin target = BirdSkinUtils.findReplacement(this.bird(), current);
      if (!target.id().equals(current.id())) {
         this.bird().getSkinController().setSkinVariant(target.id());
      }
   }
}
