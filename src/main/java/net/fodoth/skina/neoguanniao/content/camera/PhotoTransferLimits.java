package net.fodoth.skina.neoguanniao.content.camera;

public final class PhotoTransferLimits {
    public static final int IMAGE_WIDTH = 1024;
    public static final int IMAGE_HEIGHT = 1024;
    public static final int PREVIOUS_IMAGE_WIDTH = 512;
    public static final int PREVIOUS_IMAGE_HEIGHT = 512;
    public static final int LEGACY_IMAGE_WIDTH = 256;
    public static final int LEGACY_IMAGE_HEIGHT = 256;
    public static final int MAX_COMPRESSED_BYTES = 0x200000;
    public static final int MAX_CHUNK_BYTES = 24576;
    public static final int MAX_CHUNKS = 86;
    public static final int UPLOAD_TIMEOUT_TICKS = 200;
    public static final int DOWNLOAD_TIMEOUT_TICKS = 200;
    public static final int CAPTURE_COOLDOWN_TICKS = 30;
    public static final int MAX_UPLOAD_BYTES_PER_MINUTE = 0x1800000;
    public static final int MAX_PHOTO_ID_LENGTH = 80;
    public static final int SHA256_HEX_LENGTH = 64;

    private PhotoTransferLimits() {
    }

    public static boolean isCaptureDimensions(int width, int height) {
        return width == 1024 && height == 1024;
    }

    public static boolean isSupportedDimensions(int width, int height) {
        return PhotoTransferLimits.isCaptureDimensions(width, height) || width == 512 && height == 512 || width == 256 && height == 256;
    }

    public static boolean isValidPhotoId(String photoId) {
        if (photoId == null || photoId.length() < 2 || photoId.length() > 80) {
            return false;
        }
        for (int index = 0; index < photoId.length(); ++index) {
            boolean asciiLetterOrDigit;
            char character = photoId.charAt(index);
            boolean bl = asciiLetterOrDigit = character >= '0' && character <= '9' || character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z';
            if (asciiLetterOrDigit || character == '_' || character == '-') continue;
            return false;
        }
        return true;
    }
}

