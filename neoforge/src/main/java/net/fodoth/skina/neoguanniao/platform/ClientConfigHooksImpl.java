package net.fodoth.skina.neoguanniao.platform;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig;

public final class ClientConfigHooksImpl {
    private ClientConfigHooksImpl() {}
    public static boolean showCameraUi() { return NeoGuanNiaoClientConfig.SHOW_CAMERA_UI.get(); }
    public static boolean showViewfinderHint() { return NeoGuanNiaoClientConfig.SHOW_VIEWFINDER_HINT.get(); }
    public static boolean enablePreviewPostEffect() { return NeoGuanNiaoClientConfig.ENABLE_PREVIEW_POST_EFFECT.get(); }
    public static boolean enableOpticsShader() { return NeoGuanNiaoClientConfig.ENABLE_OPTICS_SHADER.get(); }
    public static boolean enableFilterPreview() { return NeoGuanNiaoClientConfig.ENABLE_FILTER_PREVIEW.get(); }
    public static boolean hideGui() { return NeoGuanNiaoClientConfig.HIDE_GUI.get(); }
    public static boolean hideHand() { return NeoGuanNiaoClientConfig.HIDE_HAND.get(); }
    public static double viewfinderOpacity() { return NeoGuanNiaoClientConfig.VIEWFINDER_OPACITY.get(); }
    public static int wheelFocusStep() { return NeoGuanNiaoClientConfig.WHEEL_FOCUS_STEP.get(); }
    public static double mouseSensitivity() { return NeoGuanNiaoClientConfig.MOUSE_SENSITIVITY.get(); }
}
