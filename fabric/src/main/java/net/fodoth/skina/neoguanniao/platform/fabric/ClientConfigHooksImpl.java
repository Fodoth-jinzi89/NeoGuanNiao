package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;

public final class ClientConfigHooksImpl {
    private ClientConfigHooksImpl() {
    }

    public static boolean showCameraUi() {
        return (boolean) NeoGuanNiaoFabricConfig.SHOW_CAMERA_UI.get();
    }

    public static boolean showViewfinderHint() {
        return (boolean) NeoGuanNiaoFabricConfig.SHOW_VIEWFINDER_HINT.get();
    }

    public static boolean enablePreviewPostEffect() {
        return (boolean) NeoGuanNiaoFabricConfig.ENABLE_PREVIEW_POST_EFFECT.get();
    }

    public static boolean enableOpticsShader() {
        return (boolean) NeoGuanNiaoFabricConfig.ENABLE_OPTICS_SHADER.get();
    }

    public static boolean enableFilterPreview() {
        return (boolean) NeoGuanNiaoFabricConfig.ENABLE_FILTER_PREVIEW.get();
    }

    public static boolean hideGui() {
        return (boolean) NeoGuanNiaoFabricConfig.HIDE_GUI.get();
    }

    public static boolean hideHand() {
        return (boolean) NeoGuanNiaoFabricConfig.HIDE_HAND.get();
    }

    public static double viewfinderOpacity() {
        return (double) NeoGuanNiaoFabricConfig.VIEWFINDER_OPACITY.get();
    }

    public static int wheelFocusStep() {
        return (int) NeoGuanNiaoFabricConfig.WHEEL_FOCUS_STEP.get();
    }

    public static double mouseSensitivity() {
        return (double) NeoGuanNiaoFabricConfig.MOUSE_SENSITIVITY.get();
    }

    public static boolean showCageRegistryName() {
        return (boolean) NeoGuanNiaoFabricConfig.BIRD_CAGES_SHOW_REGISTRY_NAME.get();
    }

    public static boolean showCageHealth() {
        return (boolean) NeoGuanNiaoFabricConfig.BIRD_CAGES_SHOW_HEALTH.get();
    }
}
