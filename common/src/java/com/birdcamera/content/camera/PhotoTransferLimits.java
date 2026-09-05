package com.birdcamera.content.camera;

/**
 * 照片传输/存储相关限制常量（迁移自 guaniao-2.1.3 相机系统）。
 */
public final class PhotoTransferLimits {
    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 256;
    public static final int MAX_COMPRESSED_BYTES = 98304;
    public static final int MAX_CHUNK_BYTES = 24576;
    public static final int MAX_CHUNKS = 4;
    public static final int UPLOAD_TIMEOUT_TICKS = 200;
    public static final int DOWNLOAD_TIMEOUT_TICKS = 200;
    public static final int CAPTURE_COOLDOWN_TICKS = 30;
    public static final int MAX_UPLOAD_BYTES_PER_MINUTE = 1179648;
    public static final int MAX_PHOTO_ID_LENGTH = 80;
    public static final int SHA256_HEX_LENGTH = 64;

    private PhotoTransferLimits() {
    }

    public static boolean isValidPhotoId(String photoId) {
        if (photoId == null || photoId.length() < 2 || photoId.length() > MAX_PHOTO_ID_LENGTH) {
            return false;
        }
        for (int index = 0; index < photoId.length(); index++) {
            char character = photoId.charAt(index);
            boolean asciiLetterOrDigit = (character >= '0' && character <= '9')
                    || (character >= 'A' && character <= 'Z')
                    || (character >= 'a' && character <= 'z');
            if (!asciiLetterOrDigit && character != '_' && character != '-') {
                return false;
            }
        }
        return true;
    }
}