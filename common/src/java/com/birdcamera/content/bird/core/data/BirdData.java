package com.birdcamera.content.bird.core.data;

import com.birdcamera.content.bird.core.data.datum.BirdAnimationDatum;
import com.birdcamera.content.bird.core.data.datum.BirdEatingDatum;
import com.birdcamera.content.bird.core.data.datum.BirdFlyingDatum;
import com.birdcamera.content.bird.core.data.datum.BirdFrightDatum;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.data.datum.BirdSoundDatum;
import com.birdcamera.content.bird.core.data.datum.BirdTameDatum;

public record BirdData(
   BirdAnimationDatum animation,
   BirdEatingDatum eating,
   BirdFlyingDatum flying,
   BirdFrightDatum fright,
   BirdMiscDatum misc,
   BirdModelSkinDatum model,
   BirdSoundDatum sound,
   BirdTameDatum tame,
   BirdGoalDatum goal
) {
   public static BirdData createDefault() {
      return new BirdData(
         BirdAnimationDatum.createDefault(),
         BirdEatingDatum.createDefault(),
         BirdFlyingDatum.createDefault(),
         BirdFrightDatum.createDefault(),
         BirdMiscDatum.createDefault(),
         BirdModelSkinDatum.createDefault(),
         BirdSoundDatum.createDefault(),
         BirdTameDatum.createDefault(),
         BirdGoalDatum.createDefault()
      );
   }

   public BirdData withAnimation(BirdAnimationDatum animation) {
      return new BirdData(animation, this.eating, this.flying, this.fright, this.misc, this.model, this.sound, this.tame, this.goal);
   }

   public BirdData withEating(BirdEatingDatum eating) {
      return new BirdData(this.animation, eating, this.flying, this.fright, this.misc, this.model, this.sound, this.tame, this.goal);
   }

   public BirdData withFlying(BirdFlyingDatum flying) {
      return new BirdData(this.animation, this.eating, flying, this.fright, this.misc, this.model, this.sound, this.tame, this.goal);
   }

   public BirdData withFright(BirdFrightDatum fright) {
      return new BirdData(this.animation, this.eating, this.flying, fright, this.misc, this.model, this.sound, this.tame, this.goal);
   }

   public BirdData withMisc(BirdMiscDatum misc) {
      return new BirdData(this.animation, this.eating, this.flying, this.fright, misc, this.model, this.sound, this.tame, this.goal);
   }

   public BirdData withModel(BirdModelSkinDatum model) {
      return new BirdData(this.animation, this.eating, this.flying, this.fright, this.misc, model, this.sound, this.tame, this.goal);
   }

   public BirdData withSound(BirdSoundDatum sound) {
      return new BirdData(this.animation, this.eating, this.flying, this.fright, this.misc, this.model, sound, this.tame, this.goal);
   }

   public BirdData withTame(BirdTameDatum tame) {
      return new BirdData(this.animation, this.eating, this.flying, this.fright, this.misc, this.model, this.sound, tame, this.goal);
   }

   public BirdData withGoal(BirdGoalDatum goal) {
      return new BirdData(this.animation, this.eating, this.flying, this.fright, this.misc, this.model, this.sound, this.tame, goal);
   }
}
