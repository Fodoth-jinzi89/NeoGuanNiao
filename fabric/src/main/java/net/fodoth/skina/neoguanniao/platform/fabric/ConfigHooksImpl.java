package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.platform.ConfigHooks;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;

public final class ConfigHooksImpl {
    private ConfigHooksImpl() {
    }

    public static ConfigHooks.Limits cameraLimits() {
        return new ConfigHooks.Limits(
                (boolean) NeoGuanNiaoFabricConfig.UPLOADS_ENABLED.get(),
                (boolean) NeoGuanNiaoFabricConfig.UPLOADS_OPERATOR_ONLY.get(),
                (boolean) NeoGuanNiaoFabricConfig.UPLOADS_WHITELISTED_ONLY.get(),
                (int) NeoGuanNiaoFabricConfig.MAX_CONCURRENT_DOWNLOADS.get(),
                (int) NeoGuanNiaoFabricConfig.DOWNLOAD_BYTES_PER_TICK.get(),
                (int) NeoGuanNiaoFabricConfig.MAX_PHOTOS_PER_PLAYER.get(),
                (long) NeoGuanNiaoFabricConfig.MAX_PHOTO_BYTES_PER_PLAYER.get(),
                (int) NeoGuanNiaoFabricConfig.MAX_PHOTOS_PER_WORLD.get(),
                (long) NeoGuanNiaoFabricConfig.MAX_PHOTO_BYTES_PER_WORLD.get(),
                (int) NeoGuanNiaoFabricConfig.PHOTO_TRASH_RETENTION_DAYS.get(),
                (int) NeoGuanNiaoFabricConfig.MAX_COMPRESSED_BYTES.get(),
                (int) NeoGuanNiaoFabricConfig.UPLOAD_TIMEOUT_TICKS.get(),
                (int) NeoGuanNiaoFabricConfig.DOWNLOAD_TIMEOUT_TICKS.get(),
                (int) NeoGuanNiaoFabricConfig.CAPTURE_COOLDOWN_TICKS.get(),
                (int) NeoGuanNiaoFabricConfig.MAX_UPLOAD_BYTES_PER_MINUTE.get());
    }
}
