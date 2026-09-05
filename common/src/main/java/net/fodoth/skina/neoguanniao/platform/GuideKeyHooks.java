package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class GuideKeyHooks {
    private GuideKeyHooks() {}
    @ExpectPlatform public static native String toggleLayoutEdit();
    @ExpectPlatform public static native String saveLayout();
    @ExpectPlatform public static native String reloadLayout();
}
