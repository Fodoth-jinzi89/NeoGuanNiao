package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;

public final class EnvironmentHooksImpl {
    private EnvironmentHooksImpl() {}

    public static boolean isProduction() {
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
