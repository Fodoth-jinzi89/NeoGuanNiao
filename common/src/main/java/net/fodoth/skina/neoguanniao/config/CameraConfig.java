package net.fodoth.skina.neoguanniao.config;

import net.fodoth.skina.neoguanniao.platform.ConfigHooks;

/** Platform-neutral camera limits backed by the active loader configuration. */
public final class CameraConfig {
    private CameraConfig() {}
    private static ConfigHooks.Limits limits() { return ConfigHooks.cameraLimits(); }
    public static boolean uploadsEnabled() { return limits().uploadsEnabled(); }
    public static boolean uploadsOperatorOnly() { return limits().uploadsOperatorOnly(); }
    public static boolean uploadsWhitelistedOnly() { return limits().uploadsWhitelistedOnly(); }
    public static int maxConcurrentDownloads() { return limits().maxConcurrentDownloads(); }
    public static int downloadBytesPerTick() { return limits().downloadBytesPerTick(); }
    public static int maxPhotosPerPlayer() { return limits().maxPhotosPerPlayer(); }
    public static long maxPhotoBytesPerPlayer() { return limits().maxPhotoBytesPerPlayer(); }
    public static int maxPhotosPerWorld() { return limits().maxPhotosPerWorld(); }
    public static long maxPhotoBytesPerWorld() { return limits().maxPhotoBytesPerWorld(); }
    public static int trashRetentionDays() { return limits().trashRetentionDays(); }
    public static int maxCompressedBytes() { return limits().maxCompressedBytes(); }
    public static int uploadTimeoutTicks() { return limits().uploadTimeoutTicks(); }
    public static int downloadTimeoutTicks() { return limits().downloadTimeoutTicks(); }
    public static int captureCooldownTicks() { return limits().captureCooldownTicks(); }
    public static int maxUploadBytesPerMinute() { return limits().maxUploadBytesPerMinute(); }
}
