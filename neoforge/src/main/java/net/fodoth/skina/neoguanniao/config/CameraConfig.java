package net.fodoth.skina.neoguanniao.config;

/**
 * Server-side safety limits for the camera system.
 *
 * <p>The values match Guaniao 3.1.4 defaults. They are centralized so storage
 * and networking enforce the same limits without duplicated constants.</p>
 */
public final class CameraConfig {

    private CameraConfig() {
    }

    public static boolean uploadsEnabled() {
        return NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_ENABLED.get();
    }

    public static boolean uploadsOperatorOnly() {
        return NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_OPERATOR_ONLY.get();
    }

    public static boolean uploadsWhitelistedOnly() {
        return NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_WHITELISTED_ONLY.get();
    }

    public static int maxConcurrentDownloads() {
        return NeoGuanNiaoNeoForgeCommonConfig.MAX_CONCURRENT_DOWNLOADS.get();
    }

    public static int downloadBytesPerTick() {
        return NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_BYTES_PER_TICK.get();
    }

    public static int maxPhotosPerPlayer() {
        return NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_PLAYER.get();
    }

    public static long maxPhotoBytesPerPlayer() {
        return NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER.get();
    }

    public static int maxPhotosPerWorld() {
        return NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_WORLD.get();
    }

    public static long maxPhotoBytesPerWorld() {
        return NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_WORLD.get();
    }

    public static int trashRetentionDays() {
        return NeoGuanNiaoNeoForgeCommonConfig.PHOTO_TRASH_RETENTION_DAYS.get();
    }

    public static int maxCompressedBytes() { return NeoGuanNiaoNeoForgeCommonConfig.MAX_COMPRESSED_BYTES.get(); }
    public static int uploadTimeoutTicks() { return NeoGuanNiaoNeoForgeCommonConfig.UPLOAD_TIMEOUT_TICKS.get(); }
    public static int downloadTimeoutTicks() { return NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_TIMEOUT_TICKS.get(); }
    public static int captureCooldownTicks() { return NeoGuanNiaoNeoForgeCommonConfig.CAPTURE_COOLDOWN_TICKS.get(); }
    public static int maxUploadBytesPerMinute() { return NeoGuanNiaoNeoForgeCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE.get(); }
}
