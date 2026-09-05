package com.birdcamera.content.bird.core.data.datum;

import net.minecraft.sounds.SoundEvent;

public record BirdSoundDatum(
   float voicePitch,
   int ambientSoundInterval,
   SoundEvent ambientSound,
   SoundEvent hurtSound,
   SoundEvent deathSound,
   SoundEvent interactionSound,
   SoundEvent eatSound
) {
   public static BirdSoundDatum createDefault() {
      return new BirdSoundDatum(0.5F, 180, null, null, null, null, null);
   }
}
