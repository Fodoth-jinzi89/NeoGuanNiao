package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class GuideKeyHooks {
    private GuideKeyHooks() {}
    @ExpectPlatform public static String toggleLayoutEdit() { return "E"; }
    @ExpectPlatform public static String saveLayout() { return "S"; }
    @ExpectPlatform public static String reloadLayout() { return "R"; }
}
