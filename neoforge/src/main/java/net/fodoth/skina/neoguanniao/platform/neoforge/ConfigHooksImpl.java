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

    public static float birdCageMaxEntitySize(int variant) {
        return switch (variant) {
            case 1 -> NeoGuanNiaoNeoForgeCommonConfig.MEDIUM_CAGE_MAX_ENTITY_SIZE.get().floatValue();
            case 2 -> NeoGuanNiaoNeoForgeCommonConfig.LARGE_CAGE_MAX_ENTITY_SIZE.get().floatValue();
            default -> NeoGuanNiaoNeoForgeCommonConfig.SMALL_CAGE_MAX_ENTITY_SIZE.get().floatValue();
        };
    }
    public static boolean birdCagesAllowHostile() { return NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_HOSTILE.get(); }
    public static boolean birdCagesAllowAllEntities() { return NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_ALL_ENTITIES.get(); }
    public static boolean birdCagesAllowNeutral() { return NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_NEUTRAL.get(); }
    public static boolean birdCagesAllowFriendly() { return NeoGuanNiaoNeoForgeCommonConfig.BIRD_CAGES_ALLOW_FRIENDLY.get(); }
}
