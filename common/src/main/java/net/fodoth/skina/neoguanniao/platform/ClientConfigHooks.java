package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ClientConfigHooks {
    private ClientConfigHooks() {}

    @ExpectPlatform public static native boolean showCameraUi();
    @ExpectPlatform public static native boolean showViewfinderHint();
    @ExpectPlatform public static native boolean enablePreviewPostEffect();
    @ExpectPlatform public static native boolean enableOpticsShader();
    @ExpectPlatform public static native boolean enableFilterPreview();
    @ExpectPlatform public static native boolean hideGui();
    @ExpectPlatform public static native boolean hideHand();
    @ExpectPlatform public static native double viewfinderOpacity();
    @ExpectPlatform public static native int wheelFocusStep();
    @ExpectPlatform public static native double mouseSensitivity();
}
