package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen.Entry;
import java.util.List;

public final class ConfigScreenHooksImpl {
    private ConfigScreenHooksImpl() {}

    // Fabric configuration persistence and screen registration are not implemented yet.
    public static List<Entry> entries() { return List.of(); }
    public static void save() {}
}
