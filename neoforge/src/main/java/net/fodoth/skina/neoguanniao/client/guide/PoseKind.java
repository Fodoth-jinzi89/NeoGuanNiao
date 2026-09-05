package net.fodoth.skina.neoguanniao.client.guide;

public enum PoseKind {
    IDLE("idle"),
    FORAGE("forage"),
    FLY("fly"),
    ALERT("alert");

    private final String key;

    PoseKind(String key) {
        this.key = key;
    }

    public String translationKey() {
        return "gui.neoguanniao.bird_guide.pose." + this.key;
    }
}