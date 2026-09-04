package net.fodoth.skina.neoguanniao.content.camera;

public enum CameraFocusMode {
    AF_S(0, "gui.neoguanniao.camera_focus_mode.af_s", "AF-S"),
    AF_C(1, "gui.neoguanniao.camera_focus_mode.af_c", "AF-C"),
    MANUAL(2, "gui.neoguanniao.camera_focus_mode.manual", "MF");

    private final int id;
    private final String translationKey;
    private final String shortName;

    private CameraFocusMode(int id, String translationKey, String shortName) {
        this.id = id;
        this.translationKey = translationKey;
        this.shortName = shortName;
    }

    public int id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public String shortName() {
        return this.shortName;
    }

    public static CameraFocusMode byId(int id) {
        for (CameraFocusMode mode : CameraFocusMode.values()) {
            if (mode.id != id) continue;
            return mode;
        }
        return AF_S;
    }
}

