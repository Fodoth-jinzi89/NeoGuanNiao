package net.fodoth.skina.neoguanniao.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server/common limits for photo storage and transfer. */
public final class NeoGuanNiaoNeoForgeCommonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue UPLOADS_ENABLED, UPLOADS_OPERATOR_ONLY, UPLOADS_WHITELISTED_ONLY;
    public static final ModConfigSpec.IntValue MAX_CONCURRENT_DOWNLOADS, DOWNLOAD_BYTES_PER_TICK, MAX_PHOTOS_PER_PLAYER,
            MAX_PHOTOS_PER_WORLD, PHOTO_TRASH_RETENTION_DAYS, MAX_COMPRESSED_BYTES,
            UPLOAD_TIMEOUT_TICKS, DOWNLOAD_TIMEOUT_TICKS, CAPTURE_COOLDOWN_TICKS, MAX_UPLOAD_BYTES_PER_MINUTE;
    public static final ModConfigSpec.LongValue MAX_PHOTO_BYTES_PER_PLAYER, MAX_PHOTO_BYTES_PER_WORLD;
    public static final ModConfigSpec.DoubleValue SMALL_CAGE_MAX_ENTITY_SIZE, MEDIUM_CAGE_MAX_ENTITY_SIZE, LARGE_CAGE_MAX_ENTITY_SIZE;
    public static final ModConfigSpec.BooleanValue BIRD_CAGES_ALLOW_HOSTILE, BIRD_CAGES_ALLOW_ALL_ENTITIES;
    public static final ModConfigSpec.BooleanValue BIRD_CAGES_ALLOW_NEUTRAL, BIRD_CAGES_ALLOW_FRIENDLY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("camera");
        UPLOADS_ENABLED = b.define("uploadsEnabled", true);
        UPLOADS_OPERATOR_ONLY = b.define("uploadsOperatorOnly", false);
        UPLOADS_WHITELISTED_ONLY = b.define("uploadsWhitelistedOnly", false);
        MAX_CONCURRENT_DOWNLOADS = b.defineInRange("maxConcurrentDownloads", 16, 1, 128);
        DOWNLOAD_BYTES_PER_TICK = b.defineInRange("downloadBytesPerTick", 196608, 4096, 1048576);
        MAX_PHOTOS_PER_PLAYER = b.defineInRange("maxPhotosPerPlayer", 1024, 1, 100000);
        MAX_PHOTO_BYTES_PER_PLAYER = b.defineInRange("maxPhotoBytesPerPlayer", 512L * 1024 * 1024, 1048576L, 16L * 1024 * 1024 * 1024);
        MAX_PHOTOS_PER_WORLD = b.defineInRange("maxPhotosPerWorld", 16384, 1, 1000000);
        MAX_PHOTO_BYTES_PER_WORLD = b.defineInRange("maxPhotoBytesPerWorld", 8L * 1024 * 1024 * 1024, 1048576L, 64L * 1024 * 1024 * 1024);
        PHOTO_TRASH_RETENTION_DAYS = b.defineInRange("photoTrashRetentionDays", 7, 0, 3650);
        MAX_COMPRESSED_BYTES = b.defineInRange("maxCompressedBytes", 0xA00000, 65536, 0x1000000);
        UPLOAD_TIMEOUT_TICKS = b.defineInRange("uploadTimeoutTicks", 200, 20, 72000);
        DOWNLOAD_TIMEOUT_TICKS = b.defineInRange("downloadTimeoutTicks", 200, 20, 72000);
        CAPTURE_COOLDOWN_TICKS = b.defineInRange("captureCooldownTicks", 30, 0, 1200);
        MAX_UPLOAD_BYTES_PER_MINUTE = b.defineInRange("maxUploadBytesPerMinute", 0x4000000, 65536, 0x10000000);
        b.pop();
        b.push("birdCages");
        SMALL_CAGE_MAX_ENTITY_SIZE = b.defineInRange("smallMaxEntitySize", 0.55D, 0.1D, 8.0D);
        MEDIUM_CAGE_MAX_ENTITY_SIZE = b.defineInRange("mediumMaxEntitySize", 1.0D, 0.1D, 8.0D);
        LARGE_CAGE_MAX_ENTITY_SIZE = b.defineInRange("largeMaxEntitySize", 2.0D, 0.1D, 8.0D);
        BIRD_CAGES_ALLOW_HOSTILE = b.define("allowHostileEntities", false);
        BIRD_CAGES_ALLOW_ALL_ENTITIES = b.define("allowAllEntities", false);
        BIRD_CAGES_ALLOW_NEUTRAL = b.define("allowNeutralEntities", false);
        BIRD_CAGES_ALLOW_FRIENDLY = b.define("allowFriendlyEntities", true);
        b.pop();
        SPEC = b.build();
    }
    private NeoGuanNiaoNeoForgeCommonConfig() {}
}
