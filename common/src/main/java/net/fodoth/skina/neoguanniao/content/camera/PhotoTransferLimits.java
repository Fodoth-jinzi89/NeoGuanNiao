package net.fodoth.skina.neoguanniao.content.camera;

public final class PhotoTransferLimits {
    private static final int CAPTURE_SIZE = 1024;
    private static final int PREVIEW_SIZE = 512;

    private PhotoTransferLimits() {
    }

    public static boolean isCaptureDimensions(int width, int height) {
        return width == CAPTURE_SIZE && height == CAPTURE_SIZE;
    }

    public static boolean isSupportedDimensions(int width, int height) {
        return isCaptureDimensions(width, height) || width == PREVIEW_SIZE && height == PREVIEW_SIZE;
    }

    public static boolean isValidPhotoId(String photoId) {
        if (photoId == null || photoId.length() < 2 || photoId.length() > 80) {
            return false;
        }
        for (int index = 0; index < photoId.length(); ++index) {
            char character = photoId.charAt(index);
            boolean ascii = character >= '0' && character <= '9' || character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z';
            if (!ascii && character != '_' && character != '-') {
                return false;
            }
        }
        return true;
    }
}

