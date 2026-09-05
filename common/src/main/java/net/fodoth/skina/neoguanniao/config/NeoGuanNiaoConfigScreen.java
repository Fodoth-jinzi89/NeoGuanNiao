package net.fodoth.skina.neoguanniao.config;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Method;

/** Optional Cloth Config screen; safely falls back when the dependency is absent. */
public final class NeoGuanNiaoConfigScreen {
    private NeoGuanNiaoConfigScreen() {}

    public static Screen create(Screen parent) {
        try {
            Class<?> type = Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            Object builder = type.getMethod("create").invoke(null);
            type.getMethod("setParentScreen", Screen.class).invoke(builder, parent);
            type.getMethod("setTitle", Component.class).invoke(builder, Component.translatable("config.neoguanniao.title"));
            Object entries = type.getMethod("entryBuilder").invoke(builder);
            Object category = type.getMethod("getOrCreateCategory", Component.class).invoke(builder, Component.translatable("config.neoguanniao.camera"));
            add(entries, category, "config.neoguanniao.camera.uploads_enabled", NeoGuanNiaoCommonConfig.UPLOADS_ENABLED);
            add(entries, category, "config.neoguanniao.camera.uploads_operator_only", NeoGuanNiaoCommonConfig.UPLOADS_OPERATOR_ONLY);
            add(entries, category, "config.neoguanniao.camera.max_photos_per_player", NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_PLAYER);
            add(entries, category, "config.neoguanniao.camera.max_photo_bytes_per_player", NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER);
            add(entries, category, "config.neoguanniao.camera.max_concurrent_downloads", NeoGuanNiaoCommonConfig.MAX_CONCURRENT_DOWNLOADS);
            add(entries, category, "config.neoguanniao.camera.download_bytes_per_tick", NeoGuanNiaoCommonConfig.DOWNLOAD_BYTES_PER_TICK);
            add(entries, category, "config.neoguanniao.camera.max_photos_per_world", NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_WORLD);
            add(entries, category, "config.neoguanniao.camera.max_photo_bytes_per_world", NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_WORLD);
            add(entries, category, "config.neoguanniao.camera.photo_trash_retention_days", NeoGuanNiaoCommonConfig.PHOTO_TRASH_RETENTION_DAYS);
            add(entries, category, "config.neoguanniao.camera.max_compressed_bytes", NeoGuanNiaoCommonConfig.MAX_COMPRESSED_BYTES);
            add(entries, category, "config.neoguanniao.camera.upload_timeout_ticks", NeoGuanNiaoCommonConfig.UPLOAD_TIMEOUT_TICKS);
            add(entries, category, "config.neoguanniao.camera.download_timeout_ticks", NeoGuanNiaoCommonConfig.DOWNLOAD_TIMEOUT_TICKS);
            add(entries, category, "config.neoguanniao.camera.capture_cooldown_ticks", NeoGuanNiaoCommonConfig.CAPTURE_COOLDOWN_TICKS);
            add(entries, category, "config.neoguanniao.camera.max_upload_bytes_per_minute", NeoGuanNiaoCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE);
            add(entries, category, "config.neoguanniao.camera.enable_optics_shader", NeoGuanNiaoClientConfig.ENABLE_OPTICS_SHADER);
            add(entries, category, "config.neoguanniao.camera.enable_filter_preview", NeoGuanNiaoClientConfig.ENABLE_FILTER_PREVIEW);
            add(entries, category, "config.neoguanniao.camera.show_viewfinder_hint", NeoGuanNiaoClientConfig.SHOW_VIEWFINDER_HINT);
            add(entries, category, "config.neoguanniao.camera.hide_gui_while_aiming", NeoGuanNiaoClientConfig.HIDE_GUI);
            add(entries, category, "config.neoguanniao.camera.hide_hand_while_aiming", NeoGuanNiaoClientConfig.HIDE_HAND);
            add(entries, category, "config.neoguanniao.camera.preview_max_size", NeoGuanNiaoClientConfig.PREVIEW_MAX_SIZE);
            add(entries, category, "config.neoguanniao.camera.wheel_focus_step", NeoGuanNiaoClientConfig.WHEEL_FOCUS_STEP);
            add(entries, category, "config.neoguanniao.camera.mouse_sensitivity", NeoGuanNiaoClientConfig.MOUSE_SENSITIVITY);
            type.getMethod("setSavingRunnable", Runnable.class).invoke(builder, (Runnable) () -> { NeoGuanNiaoCommonConfig.SPEC.save(); NeoGuanNiaoClientConfig.SPEC.save(); });
            return (Screen) type.getMethod("build").invoke(builder);
        } catch (Exception e) {
            NeoGuanNiao.LOGGER.warn("Cloth Config is unavailable; using default config screen", e);
            return parent;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void add(Object entries, Object category, String key, Object config) throws Exception {
        boolean bool = config instanceof ModConfigSpec.BooleanValue;
        boolean integer = config instanceof ModConfigSpec.IntValue;
        boolean decimal = config instanceof ModConfigSpec.DoubleValue;
        String method = bool ? "startBooleanToggle" : integer ? "startIntField" : decimal ? "startDoubleField" : "startLongField";
        Class<?> valueType = bool ? boolean.class : integer ? int.class : decimal ? double.class : long.class;
        Object value = ((ModConfigSpec.ConfigValue) config).get();
        Object field = entries.getClass().getMethod(method, Component.class, valueType).invoke(entries, Component.translatable(key), value);
        field.getClass().getMethod("setSaveConsumer", java.util.function.Consumer.class).invoke(field, (java.util.function.Consumer<Object>) ((ModConfigSpec.ConfigValue) config)::set);
        Object entry = field.getClass().getMethod("build").invoke(field);
        for (Method m : category.getClass().getMethods()) if (m.getName().equals("addEntry") && m.getParameterCount() == 1) { m.invoke(category, entry); return; }
    }
}
