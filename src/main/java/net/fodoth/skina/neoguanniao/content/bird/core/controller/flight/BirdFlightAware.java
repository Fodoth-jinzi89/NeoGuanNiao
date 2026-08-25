package net.fodoth.skina.neoguanniao.content.bird.core.controller.flight;

import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlightProfile;


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

