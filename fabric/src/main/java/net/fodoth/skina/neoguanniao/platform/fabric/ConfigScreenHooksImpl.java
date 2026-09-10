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
    /** 归入「鸟笼」分类的 Fabric 设置键前缀（camelCase 转 snake_case 之后）。 */
    private static final String BIRD_CAGE_PREFIX = "bird_cages_";

    private ConfigScreenHooksImpl() {}

    public static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (var setting : NeoGuanNiaoFabricConfig.settings()) {
            String name = setting.key().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
            // 鸟笼开关归到「鸟笼」分类，其余留在「相机」分类。
            String key = name.startsWith(BIRD_CAGE_PREFIX)
                    ? "config.neoguanniao.bird_cages." + name.substring(BIRD_CAGE_PREFIX.length())
                    : "config.neoguanniao.camera." + name;
            entries.add(new Entry(key, setting.type(), setting::get, setting::set));
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
