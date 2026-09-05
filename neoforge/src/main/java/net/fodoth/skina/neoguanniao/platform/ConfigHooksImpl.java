package net.fodoth.skina.neoguanniao.platform;

import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoCommonConfig;

public final class ConfigHooksImpl {
    private ConfigHooksImpl() {}

    public static ConfigHooks.Limits cameraLimits() {
        return new ConfigHooks.Limits(
                NeoGuanNiaoCommonConfig.UPLOADS_ENABLED.get(),
                NeoGuanNiaoCommonConfig.UPLOADS_OPERATOR_ONLY.get(),
                NeoGuanNiaoCommonConfig.UPLOADS_WHITELISTED_ONLY.get(),
                NeoGuanNiaoCommonConfig.MAX_CONCURRENT_DOWNLOADS.get(),
                NeoGuanNiaoCommonConfig.DOWNLOAD_BYTES_PER_TICK.get(),
                NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_PLAYER.get(),
                NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER.get(),
                NeoGuanNiaoCommonConfig.MAX_PHOTOS_PER_WORLD.get(),
                NeoGuanNiaoCommonConfig.MAX_PHOTO_BYTES_PER_WORLD.get(),
                NeoGuanNiaoCommonConfig.PHOTO_TRASH_RETENTION_DAYS.get(),
                NeoGuanNiaoCommonConfig.MAX_COMPRESSED_BYTES.get(),
                NeoGuanNiaoCommonConfig.UPLOAD_TIMEOUT_TICKS.get(),
                NeoGuanNiaoCommonConfig.DOWNLOAD_TIMEOUT_TICKS.get(),
                NeoGuanNiaoCommonConfig.CAPTURE_COOLDOWN_TICKS.get(),
                NeoGuanNiaoCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE.get());
    }
}
