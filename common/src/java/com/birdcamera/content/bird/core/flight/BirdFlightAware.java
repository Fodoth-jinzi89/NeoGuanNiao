package com.birdcamera.content.bird.core.flight;

import com.birdcamera.content.bird.core.data.datum.BirdFlightProfile;

public interface BirdFlightAware {
   BirdFlightProfile birdFlightProfile();

   boolean isBirdFlightActive();

   default boolean isBirdLanding() {
      return false;
   }

   default boolean isBirdEscaping() {
      return false;
   }
}
