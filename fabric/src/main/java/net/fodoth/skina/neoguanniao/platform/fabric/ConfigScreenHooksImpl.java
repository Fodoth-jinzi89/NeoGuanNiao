package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen.Entry;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConfigScreenHooksImpl {
    private ConfigScreenHooksImpl() {}

    public static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (var setting : NeoGuanNiaoFabricConfig.settings()) {
            String key = setting.key().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            entries.add(new Entry("config.neoguanniao.camera." + key, setting.type(), setting::get, setting::set));
        }
        return entries;
    }

    public static void save() {
        try {
            NeoGuanNiaoFabricConfig.save(FabricLoader.getInstance().getConfigDir().resolve("neoguanniao.properties"));
        } catch (IOException exception) {
            NeoGuanNiao.LOGGER.error("Unable to save Fabric camera configuration", exception);
            throw new UncheckedIOException(exception);
        }
    }
}
