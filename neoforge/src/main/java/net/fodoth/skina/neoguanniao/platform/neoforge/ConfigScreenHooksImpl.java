package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen.Entry;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeCommonConfig;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public final class ConfigScreenHooksImpl {
    private ConfigScreenHooksImpl() {}

    public static List<Entry> entries() {
        return List.of(
                entry("config.neoguanniao.camera.uploads_enabled", NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_ENABLED),
                entry("config.neoguanniao.camera.uploads_operator_only", NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_OPERATOR_ONLY),
                entry("config.neoguanniao.camera.max_photos_per_player", NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_PLAYER),
                entry("config.neoguanniao.camera.max_photo_bytes_per_player", NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER),
                entry("config.neoguanniao.camera.max_concurrent_downloads", NeoGuanNiaoNeoForgeCommonConfig.MAX_CONCURRENT_DOWNLOADS),
                entry("config.neoguanniao.camera.download_bytes_per_tick", NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_BYTES_PER_TICK),
                entry("config.neoguanniao.camera.max_photos_per_world", NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_WORLD),
                entry("config.neoguanniao.camera.max_photo_bytes_per_world", NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_WORLD),
                entry("config.neoguanniao.camera.photo_trash_retention_days", NeoGuanNiaoNeoForgeCommonConfig.PHOTO_TRASH_RETENTION_DAYS),
                entry("config.neoguanniao.camera.max_compressed_bytes", NeoGuanNiaoNeoForgeCommonConfig.MAX_COMPRESSED_BYTES),
                entry("config.neoguanniao.camera.upload_timeout_ticks", NeoGuanNiaoNeoForgeCommonConfig.UPLOAD_TIMEOUT_TICKS),
                entry("config.neoguanniao.camera.download_timeout_ticks", NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_TIMEOUT_TICKS),
                entry("config.neoguanniao.camera.capture_cooldown_ticks", NeoGuanNiaoNeoForgeCommonConfig.CAPTURE_COOLDOWN_TICKS),
                entry("config.neoguanniao.camera.max_upload_bytes_per_minute", NeoGuanNiaoNeoForgeCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE),
                entry("config.neoguanniao.camera.enable_optics_shader", NeoGuanNiaoNeoForgeClientConfig.ENABLE_OPTICS_SHADER),
                entry("config.neoguanniao.camera.enable_filter_preview", NeoGuanNiaoNeoForgeClientConfig.ENABLE_FILTER_PREVIEW),
                entry("config.neoguanniao.camera.show_viewfinder_hint", NeoGuanNiaoNeoForgeClientConfig.SHOW_VIEWFINDER_HINT),
                entry("config.neoguanniao.camera.hide_gui_while_aiming", NeoGuanNiaoNeoForgeClientConfig.HIDE_GUI),
                entry("config.neoguanniao.camera.hide_hand_while_aiming", NeoGuanNiaoNeoForgeClientConfig.HIDE_HAND),
                entry("config.neoguanniao.camera.preview_max_size", NeoGuanNiaoNeoForgeClientConfig.PREVIEW_MAX_SIZE),
                entry("config.neoguanniao.camera.wheel_focus_step", NeoGuanNiaoNeoForgeClientConfig.WHEEL_FOCUS_STEP),
                entry("config.neoguanniao.camera.mouse_sensitivity", NeoGuanNiaoNeoForgeClientConfig.MOUSE_SENSITIVITY),
                entry("config.neoguanniao.bird_cages.small_max_entity_size", NeoGuanNiaoNeoForgeCommonConfig.SMALL_CAGE_MAX_ENTITY_SIZE),
                entry("config.neoguanniao.bird_cages.medium_max_entity_size", NeoGuanNiaoNeoForgeCommonConfig.MEDIUM_CAGE_MAX_ENTITY_SIZE),
                entry("config.neoguanniao.bird_cages.large_max_entity_size", NeoGuanNiaoNeoForgeCommonConfig.LARGE_CAGE_MAX_ENTITY_SIZE),
                entry("config.neoguanniao.bird_cages.allow_hostile", NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_HOSTILE),
                entry("config.neoguanniao.bird_cages.allow_neutral", NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_NEUTRAL),
                entry("config.neoguanniao.bird_cages.allow_friendly", NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_FRIENDLY),
                entry("config.neoguanniao.bird_cages.allow_all", NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_ALL_ENTITIES));
    }

    private static <T> Entry entry(String key, ModConfigSpec.ConfigValue<T> config) {
        Class<?> type = config instanceof ModConfigSpec.BooleanValue ? boolean.class
                : config instanceof ModConfigSpec.IntValue ? int.class
                : config instanceof ModConfigSpec.DoubleValue ? double.class : long.class;
        return new Entry(key, type, config::get, value -> set(config, value));
    }

    @SuppressWarnings("unchecked")
    private static <T> void set(ModConfigSpec.ConfigValue<T> config, Object value) {
        config.set((T) value);
    }

    public static void save() {
        NeoGuanNiaoNeoForgeCommonConfig.SPEC.save();
        NeoGuanNiaoNeoForgeClientConfig.SPEC.save();
    }
}
