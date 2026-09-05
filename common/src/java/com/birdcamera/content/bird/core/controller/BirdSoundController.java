package com.birdcamera.content.bird.core.controller;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdSoundDatum;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

public class BirdSoundController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   @Nullable
   public SoundEvent getAmbientSound() {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.ambientSound();
   }

   @Nullable
   public SoundEvent getHurtSound(DamageSource source) {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.hurtSound();
   }

   @Nullable
   public SoundEvent getDeathSound() {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.deathSound();
   }

   public float getVoicePitch() {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.voicePitch();
   }

   public int getAmbientSoundInterval() {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.ambientSoundInterval();
   }

   @Nullable
   public SoundEvent getInteractionSound() {
      BirdData birdData = this.bird.getBirdData();
      BirdSoundDatum soundDatum = birdData.sound();
      return soundDatum.interactionSound();
   }
}
