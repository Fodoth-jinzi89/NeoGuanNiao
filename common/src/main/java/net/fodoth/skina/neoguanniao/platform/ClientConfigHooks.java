package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ClientConfigHooks {
    private ClientConfigHooks() {}

    @ExpectPlatform public static boolean showCameraUi() { return true; }
    @ExpectPlatform public static boolean showViewfinderHint() { return true; }
    @ExpectPlatform public static boolean enablePreviewPostEffect() { return true; }
    @ExpectPlatform public static boolean enableOpticsShader() { return true; }
    @ExpectPlatform public static boolean enableFilterPreview() { return true; }
    @ExpectPlatform public static boolean hideGui() { return false; }
    @ExpectPlatform public static boolean hideHand() { return false; }
    @ExpectPlatform public static double viewfinderOpacity() { return 0.9; }
    @ExpectPlatform public static int wheelFocusStep() { return 1; }
    @ExpectPlatform public static double mouseSensitivity() { return 1.0; }
    @ExpectPlatform public static boolean showCageRegistryName() { return false; }
    @ExpectPlatform public static boolean showCageHealth() { return true; }
}
