package net.fodoth.skina.neoguanniao.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client presentation and camera-control preferences. */
public final class NeoGuanNiaoClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_VIEWFINDER_HINT, SHOW_CAMERA_UI, ENABLE_OPTICS_SHADER,
            ENABLE_FILTER_PREVIEW, HIDE_GUI, HIDE_HAND, ENABLE_PREVIEW_POST_EFFECT;
    public static final ModConfigSpec.DoubleValue VIEWFINDER_OPACITY, MOUSE_SENSITIVITY;
    public static final ModConfigSpec.IntValue PREVIEW_MAX_SIZE, WHEEL_FOCUS_STEP;
    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("camera");
        SHOW_VIEWFINDER_HINT = b.define("showViewfinderHint", true);
        SHOW_CAMERA_UI = b.define("showCameraUi", true);
        ENABLE_OPTICS_SHADER = b.define("enableOpticsShader", true);
        ENABLE_FILTER_PREVIEW = b.define("enableFilterPreview", true);
        VIEWFINDER_OPACITY = b.defineInRange("viewfinderOpacity", 0.92D, 0.1D, 1.0D);
        PREVIEW_MAX_SIZE = b.defineInRange("previewMaxSize", 512, 64, 1024);
        HIDE_GUI = b.define("hideGuiWhileAiming", true);
        HIDE_HAND = b.define("hideHandWhileAiming", true);
        WHEEL_FOCUS_STEP = b.defineInRange("wheelFocusStep", 1, 1, 32);
        MOUSE_SENSITIVITY = b.defineInRange("mouseSensitivity", 1.0D, 0.1D, 4.0D);
        ENABLE_PREVIEW_POST_EFFECT = b.define("enablePreviewPostEffect", true);
        b.pop();
        SPEC = b.build();
    }
    private NeoGuanNiaoClientConfig() {}
}
