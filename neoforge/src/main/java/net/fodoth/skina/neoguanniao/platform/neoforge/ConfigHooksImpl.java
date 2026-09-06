package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.platform.ConfigHooks;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeCommonConfig;

public final class ConfigHooksImpl {
    private ConfigHooksImpl() {}

    public static ConfigHooks.Limits cameraLimits() {
        return new ConfigHooks.Limits(
                NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_ENABLED.get(),
                NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_OPERATOR_ONLY.get(),
                NeoGuanNiaoNeoForgeCommonConfig.UPLOADS_WHITELISTED_ONLY.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_CONCURRENT_DOWNLOADS.get(),
                NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_BYTES_PER_TICK.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_PLAYER.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_PLAYER.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTOS_PER_WORLD.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_PHOTO_BYTES_PER_WORLD.get(),
                NeoGuanNiaoNeoForgeCommonConfig.PHOTO_TRASH_RETENTION_DAYS.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_COMPRESSED_BYTES.get(),
                NeoGuanNiaoNeoForgeCommonConfig.UPLOAD_TIMEOUT_TICKS.get(),
                NeoGuanNiaoNeoForgeCommonConfig.DOWNLOAD_TIMEOUT_TICKS.get(),
                NeoGuanNiaoNeoForgeCommonConfig.CAPTURE_COOLDOWN_TICKS.get(),
                NeoGuanNiaoNeoForgeCommonConfig.MAX_UPLOAD_BYTES_PER_MINUTE.get());
    }
}
