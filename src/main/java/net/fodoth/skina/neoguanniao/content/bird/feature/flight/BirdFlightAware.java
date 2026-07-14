package net.fodoth.skina.neoguanniao.content.bird.feature.flight;


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

