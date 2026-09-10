package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen.Entry;
import java.util.List;

public final class ConfigScreenHooks {
    private ConfigScreenHooks() {}

    @ExpectPlatform
    public static List<Entry> entries() { throw new AssertionError(); }

    @ExpectPlatform
    public static void save() { throw new AssertionError(); }
}
