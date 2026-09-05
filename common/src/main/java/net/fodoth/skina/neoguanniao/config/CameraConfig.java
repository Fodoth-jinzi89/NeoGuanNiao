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
        return NeoGuanNiaoCommonConfig.UPLOADS_ENABLED.get();
    }

    public static boolean uploadsOperatorOnly() {
        return NeoGuanNiaoCommonConfig.UPLOADS_OPERATOR_ONLY.get();
    }

    public static boolean uploadsWhitelistedOnly() {
        return NeoGuanNiaoCommonConfig.UPLOADS_WHITELISTED_ONLY.get();
    }

    public static int maxConcurrentDownloads() {
        return NeoGuanNiaoCommonConfig.MAX_CONCURRENT_DOWNLOADS.get();
    }

    public static int downloadBytesPerTick() {
        return NeoGuanNiaoCommonConfig.DOWNLOAD_BYTES_PER_TICK.get();
    }

    public static int maxPhotosPerPlayer() {
        return NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_PLAYER.get();
    }

    public static long maxPhotoBytesPerPlayer() {
        return NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER.get();
    }

    public static int maxPhotosPerWorld() {
        return NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_WORLD.get();
    }

    public static long maxPhotoBytesPerWorld() {
        return NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_WORLD.get();
    }

    public static int trashRetentionDays() {
        return NeoGuanNiaoCommonConfig.PHOTO_TRASH_RETENTION_DAYS.get();
    }

    public static int maxCompressedBytes() { return NeoGuanNiaoCommonConfig.MAX_COMPRESSED_BYTES.get(); }
    public static int uploadTimeoutTicks() { return NeoGuanNiaoCommonConfig.UPLOAD_TIMEOUT_TICKS.get(); }
    public static int downloadTimeoutTicks() { return NeoGuanNiaoCommonConfig.DOWNLOAD_TIMEOUT_TICKS.get(); }
    public static int captureCooldownTicks() { return NeoGuanNiaoCommonConfig.CAPTURE_COOLDOWN_TICKS.get(); }
    public static int maxUploadBytesPerMinute() { return NeoGuanNiaoCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE.get(); }
}
