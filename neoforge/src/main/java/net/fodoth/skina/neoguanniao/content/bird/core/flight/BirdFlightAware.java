package net.fodoth.skina.neoguanniao.content.bird.core.flight;

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

