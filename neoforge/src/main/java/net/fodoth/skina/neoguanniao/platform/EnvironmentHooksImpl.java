package net.fodoth.skina.neoguanniao.platform;

import net.neoforged.fml.loading.FMLEnvironment;

public final class EnvironmentHooksImpl {
    private EnvironmentHooksImpl() {}

    public static boolean isProduction() {
        return FMLEnvironment.production;
    }
}
