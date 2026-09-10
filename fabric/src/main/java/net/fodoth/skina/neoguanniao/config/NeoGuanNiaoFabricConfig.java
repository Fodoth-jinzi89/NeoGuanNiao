package net.fodoth.skina.neoguanniao.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Fabric camera settings, with the same defaults and bounds as NeoForge. */
public final class NeoGuanNiaoFabricConfig {
    private static final Map<String, Setting> SETTINGS = new LinkedHashMap<>();
    private static final Properties VALUES = new Properties();
    public static final Setting UPLOADS_ENABLED = define("uploadsEnabled", boolean.class, true, 0, 1);
    public static final Setting UPLOADS_OPERATOR_ONLY = define("uploadsOperatorOnly", boolean.class, false, 0, 1);
    public static final Setting UPLOADS_WHITELISTED_ONLY = define("uploadsWhitelistedOnly", boolean.class, false, 0, 1);
    public static final Setting MAX_CONCURRENT_DOWNLOADS = define("maxConcurrentDownloads", int.class, 16, 1, 128);
    public static final Setting DOWNLOAD_BYTES_PER_TICK = define("downloadBytesPerTick", int.class, 196608, 4096, 1048576);
    public static final Setting MAX_PHOTOS_PER_PLAYER = define("maxPhotosPerPlayer", int.class, 1024, 1, 100000);
    public static final Setting MAX_PHOTO_BYTES_PER_PLAYER = define("maxPhotoBytesPerPlayer", long.class, 512L * 1024 * 1024, 1048576L, 16L * 1024 * 1024 * 1024);
    public static final Setting MAX_PHOTOS_PER_WORLD = define("maxPhotosPerWorld", int.class, 16384, 1, 1000000);
    public static final Setting MAX_PHOTO_BYTES_PER_WORLD = define("maxPhotoBytesPerWorld", long.class, 8L * 1024 * 1024 * 1024, 1048576L, 64L * 1024 * 1024 * 1024);
    public static final Setting PHOTO_TRASH_RETENTION_DAYS = define("photoTrashRetentionDays", int.class, 7, 0, 3650);
    public static final Setting MAX_COMPRESSED_BYTES = define("maxCompressedBytes", int.class, 0xA00000, 65536, 0x1000000);
    public static final Setting UPLOAD_TIMEOUT_TICKS = define("uploadTimeoutTicks", int.class, 200, 20, 72000);
    public static final Setting DOWNLOAD_TIMEOUT_TICKS = define("downloadTimeoutTicks", int.class, 200, 20, 72000);
    public static final Setting CAPTURE_COOLDOWN_TICKS = define("captureCooldownTicks", int.class, 30, 0, 1200);
    public static final Setting MAX_UPLOAD_BYTES_PER_MINUTE = define("maxUploadBytesPerMinute", int.class, 0x4000000, 65536, 0x10000000);
    public static final Setting SHOW_VIEWFINDER_HINT = define("showViewfinderHint", boolean.class, true, 0, 1);
    public static final Setting SHOW_CAMERA_UI = define("showCameraUi", boolean.class, true, 0, 1);
    public static final Setting ENABLE_OPTICS_SHADER = define("enableOpticsShader", boolean.class, true, 0, 1);
    public static final Setting ENABLE_FILTER_PREVIEW = define("enableFilterPreview", boolean.class, true, 0, 1);
    public static final Setting VIEWFINDER_OPACITY = define("viewfinderOpacity", double.class, 0.92D, 0.1D, 1.0D);
    public static final Setting PREVIEW_MAX_SIZE = define("previewMaxSize", int.class, 512, 64, 1024);
    public static final Setting HIDE_GUI = define("hideGuiWhileAiming", boolean.class, true, 0, 1);
    public static final Setting HIDE_HAND = define("hideHandWhileAiming", boolean.class, true, 0, 1);
    public static final Setting WHEEL_FOCUS_STEP = define("wheelFocusStep", int.class, 1, 1, 32);
    public static final Setting MOUSE_SENSITIVITY = define("mouseSensitivity", double.class, 1.0D, 0.1D, 4.0D);
    public static final Setting ENABLE_PREVIEW_POST_EFFECT = define("enablePreviewPostEffect", boolean.class, true, 0, 1);
    public static final Setting BIRD_CAGES_SHOW_REGISTRY_NAME = define("birdCagesShowRegistryName", boolean.class, false, 0, 1);
    public static final Setting BIRD_CAGES_SHOW_HEALTH = define("birdCagesShowHealth", boolean.class, true, 0, 1);

    private NeoGuanNiaoFabricConfig() {}

    private static Setting define(String key, Class<?> type, Object fallback, double min, double max) {
        Setting setting = new Setting(key, type, fallback, min, max);
        SETTINGS.put(key, setting);
        return setting;
    }

    public static void load(Path path) throws IOException {
        Properties loaded = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                loaded.load(reader);
            }
        }
        VALUES.clear();
        VALUES.putAll(loaded);
        for (Setting setting : SETTINGS.values()) setting.set(setting.get());
    }

    public static void save(Path path) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(path.toAbsolutePath().getParent(), "neoguanniao-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                VALUES.store(writer, "Neo Guan Niao camera settings; server limits apply on the host only");
            }
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static Iterable<Setting> settings() { return SETTINGS.values(); }

    public record Setting(String key, Class<?> type, Object fallback, double min, double max) {
        public Object get() {
            String raw = VALUES.getProperty(key, fallback.toString());
            try {
                if (type == boolean.class) {
                    if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) return fallback;
                    return Boolean.parseBoolean(raw);
                }
                double value = Double.parseDouble(raw);
                if (!Double.isFinite(value) || value < min || value > max) return fallback;
                if (type == double.class) return value;
                if (type == int.class) return Integer.parseInt(raw);
                return Long.parseLong(raw);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        public void set(Object value) {
            VALUES.setProperty(key, String.valueOf(value));
            VALUES.setProperty(key, get().toString());
        }
    }
}
