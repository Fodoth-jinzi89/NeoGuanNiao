package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ConfigHooks {
    private ConfigHooks() {}

    @ExpectPlatform
    public static native Limits cameraLimits();

    public record Limits(boolean uploadsEnabled, boolean uploadsOperatorOnly, boolean uploadsWhitelistedOnly,
                         int maxConcurrentDownloads, int downloadBytesPerTick, int maxPhotosPerPlayer,
                         long maxPhotoBytesPerPlayer, int maxPhotosPerWorld, long maxPhotoBytesPerWorld,
                         int trashRetentionDays, int maxCompressedBytes, int uploadTimeoutTicks,
                         int downloadTimeoutTicks, int captureCooldownTicks, int maxUploadBytesPerMinute) {
        public static Limits defaults() {
            return new Limits(true, false, false, 2, 65536, 100, 104857600L, 1000, 1073741824L,
                    7, 10485760, 600, 600, 20, 104857600);
        }
    }
}
