package net.fodoth.skina.neoguanniao.platform;

public final class ClientConfigHooksImpl {
    private ClientConfigHooksImpl() {}
    public static boolean showCameraUi() { return true; }
    public static boolean showViewfinderHint() { return true; }
    public static boolean enablePreviewPostEffect() { return true; }
    public static boolean enableOpticsShader() { return true; }
    public static boolean enableFilterPreview() { return true; }
    public static boolean hideGui() { return true; }
    public static boolean hideHand() { return true; }
    public static double viewfinderOpacity() { return 0.92D; }
    public static int wheelFocusStep() { return 1; }
    public static double mouseSensitivity() { return 1.0D; }
}
