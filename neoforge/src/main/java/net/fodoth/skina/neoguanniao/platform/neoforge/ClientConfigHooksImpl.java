package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeClientConfig;

public final class ClientConfigHooksImpl {
    private ClientConfigHooksImpl() {}
    public static boolean showCameraUi() { return NeoGuanNiaoNeoForgeClientConfig.SHOW_CAMERA_UI.get(); }
    public static boolean showViewfinderHint() { return NeoGuanNiaoNeoForgeClientConfig.SHOW_VIEWFINDER_HINT.get(); }
    public static boolean enablePreviewPostEffect() { return NeoGuanNiaoNeoForgeClientConfig.ENABLE_PREVIEW_POST_EFFECT.get(); }
    public static boolean enableOpticsShader() { return NeoGuanNiaoNeoForgeClientConfig.ENABLE_OPTICS_SHADER.get(); }
    public static boolean enableFilterPreview() { return NeoGuanNiaoNeoForgeClientConfig.ENABLE_FILTER_PREVIEW.get(); }
    public static boolean hideGui() { return NeoGuanNiaoNeoForgeClientConfig.HIDE_GUI.get(); }
    public static boolean hideHand() { return NeoGuanNiaoNeoForgeClientConfig.HIDE_HAND.get(); }
    public static double viewfinderOpacity() { return NeoGuanNiaoNeoForgeClientConfig.VIEWFINDER_OPACITY.get(); }
    public static int wheelFocusStep() { return NeoGuanNiaoNeoForgeClientConfig.WHEEL_FOCUS_STEP.get(); }
    public static double mouseSensitivity() { return NeoGuanNiaoNeoForgeClientConfig.MOUSE_SENSITIVITY.get(); }
}
