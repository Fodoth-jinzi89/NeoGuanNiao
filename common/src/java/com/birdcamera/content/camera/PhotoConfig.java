package com.birdcamera.content.camera;

/**
 * 相机/照片功能配置（常量形式，替换原 guaniao 的 BirdConfigManager 配置项，
 * 保持默认即是"完全可用"的状态）。
 */
public final class PhotoConfig {
    /** 是否允许上传照片 */
    public static final boolean PHOTO_UPLOADS_ENABLED = true;
    /** 是否仅管理员可拍照 */
    public static final boolean PHOTO_UPLOADS_OPERATOR_ONLY = false;
    /** 是否仅白名单可拍照 */
    public static final boolean PHOTO_UPLOADS_WHITELISTED_ONLY = false;
    /** 每名玩家的最大照片数量 */
    public static final int MAX_PHOTOS_PER_PLAYER = 200;
    /** 每名玩家的照片总容量（字节） */
    public static final long MAX_PHOTO_BYTES_PER_PLAYER = 64L * 1024 * 1024;
    /** 世界的最大照片数量 */
    public static final int MAX_PHOTOS_PER_WORLD = 10000;
    /** 世界的照片总容量（字节） */
    public static final long MAX_PHOTO_BYTES_PER_WORLD = 512L * 1024 * 1024;
    /** 回收站保留天数 */
    public static final int PHOTO_TRASH_RETENTION_DAYS = 14;
    /** 全服并发照片下载数 */
    public static final int MAX_CONCURRENT_PHOTO_DOWNLOADS = 8;
    /** 每个游戏刻最多发送的照片字节数 */
    public static final int PHOTO_DOWNLOAD_BYTES_PER_TICK = 2 * 1024;

    /** 拍照冷却（游戏刻） */
    public static final int CAPTURE_COOLDOWN_TICKS = PhotoTransferLimits.CAPTURE_COOLDOWN_TICKS;
    /** 上传超时（游戏刻） */
    public static final int UPLOAD_TIMEOUT_TICKS = PhotoTransferLimits.UPLOAD_TIMEOUT_TICKS;
    /** 下载超时（游戏刻） */
    public static final int DOWNLOAD_TIMEOUT_TICKS = PhotoTransferLimits.DOWNLOAD_TIMEOUT_TICKS;
    /** 单包最大字节 */
    public static final int MAX_CHUNK_BYTES = PhotoTransferLimits.MAX_CHUNK_BYTES;

    private PhotoConfig() {
    }

    public static boolean photoUploadsEnabled() {
        return PHOTO_UPLOADS_ENABLED;
    }

    public static boolean photoUploadsOperatorOnly() {
        return PHOTO_UPLOADS_OPERATOR_ONLY;
    }

    public static boolean photoUploadsWhitelistedOnly() {
        return PHOTO_UPLOADS_WHITELISTED_ONLY;
    }

    public static int maxPhotosPerPlayer() {
        return MAX_PHOTOS_PER_PLAYER;
    }

    public static long maxPhotoBytesPerPlayer() {
        return MAX_PHOTO_BYTES_PER_PLAYER;
    }

    public static int maxPhotosPerWorld() {
        return MAX_PHOTOS_PER_WORLD;
    }

    public static long maxPhotoBytesPerWorld() {
        return MAX_PHOTO_BYTES_PER_WORLD;
    }

    public static int photoTrashRetentionDays() {
        return PHOTO_TRASH_RETENTION_DAYS;
    }

    public static int maxConcurrentPhotoDownloads() {
        return MAX_CONCURRENT_PHOTO_DOWNLOADS;
    }

    public static int photoDownloadBytesPerTick() {
        return PHOTO_DOWNLOAD_BYTES_PER_TICK;
    }
}