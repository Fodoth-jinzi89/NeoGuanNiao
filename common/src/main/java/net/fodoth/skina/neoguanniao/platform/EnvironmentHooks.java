package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class EnvironmentHooks {
    private EnvironmentHooks() {}

    @ExpectPlatform
    public static native boolean isProduction();
}
