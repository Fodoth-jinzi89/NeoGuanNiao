package net.fodoth.skina.neoguanniao.content.camera;

public enum CameraAperture {
    F1_4(0, 1.4f, "F1.4"),
    F2(1, 2.0f, "F2"),
    F2_8(2, 2.8f, "F2.8"),
    F4(3, 4.0f, "F4"),
    F5_6(4, 5.6f, "F5.6"),
    F8(5, 8.0f, "F8"),
    F11(6, 11.0f, "F11"),
    F16(7, 16.0f, "F16");

    private final int id;
    private final float fStop;
    private final String label;

    private CameraAperture(int id, float fStop, String label) {
        this.id = id;
        this.fStop = fStop;
        this.label = label;
    }

    public int id() {
        return this.id;
    }

    public float fStop() {
        return this.fStop;
    }

    public String label() {
        return this.label;
    }

    public CameraAperture next() {
        CameraAperture[] values = CameraAperture.values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public CameraAperture previous() {
        CameraAperture[] values = CameraAperture.values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }

    public static CameraAperture byId(int id) {
        for (CameraAperture aperture : CameraAperture.values()) {
            if (aperture.id != id) continue;
            return aperture;
        }
        return F5_6;
    }
}

