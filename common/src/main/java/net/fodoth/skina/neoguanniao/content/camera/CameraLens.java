package net.fodoth.skina.neoguanniao.content.camera;

public enum CameraLens {
    STANDARD(0, "gui.neoguanniao.camera_lens.standard", 50.0, 0.0f, 1.0f, 0.5),
    WIDE(1, "gui.neoguanniao.camera_lens.wide", 24.0, 0.12f, 0.72f, 0.5),
    TELEPHOTO(2, "gui.neoguanniao.camera_lens.telephoto", 200.0, -0.015f, 1.55f, 4.0),
    MACRO(3, "gui.neoguanniao.camera_lens.macro", 100.0, 0.0f, 1.85f, 0.3),
    FISHEYE(4, "gui.neoguanniao.camera_lens.fisheye", 8.0, 0.78f, 0.68f, 0.5);

    private final int id;
    private final String translationKey;
    private final double defaultFocalLength;
    private final float distortion;
    private final float depthOfFieldMultiplier;
    private final double minimumFocusDistance;

    private CameraLens(int id, String translationKey, double defaultFocalLength, float distortion, float depthOfFieldMultiplier, double minimumFocusDistance) {
        this.id = id;
        this.translationKey = translationKey;
        this.defaultFocalLength = defaultFocalLength;
        this.distortion = distortion;
        this.depthOfFieldMultiplier = depthOfFieldMultiplier;
        this.minimumFocusDistance = minimumFocusDistance;
    }

    public int id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public String descriptionKey() {
        return this.translationKey + ".description";
    }

    public double defaultFocalLength() {
        return this.defaultFocalLength;
    }

    public float distortion() {
        return this.distortion;
    }

    public float depthOfFieldMultiplier() {
        return this.depthOfFieldMultiplier;
    }

    public double minimumFocusDistance() {
        return this.minimumFocusDistance;
    }

    public static CameraLens byId(int id) {
        for (CameraLens lens : CameraLens.values()) {
            if (lens.id != id) continue;
            return lens;
        }
        return STANDARD;
    }
}

