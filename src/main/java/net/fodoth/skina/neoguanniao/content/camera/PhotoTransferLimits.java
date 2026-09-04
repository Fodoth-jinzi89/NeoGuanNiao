package net.fodoth.skina.neoguanniao.content.camera;

public final class PhotoTransferLimits {

    private PhotoTransferLimits() {
    }

    public static boolean isCaptureDimensions(int width, int height) {
        return width == 1024 && height == 1024;
    }

    public static boolean isSupportedDimensions(int width, int height) {
        return PhotoTransferLimits.isCaptureDimensions(width, height) || width == 512 && height == 512;
    }

    public static boolean isValidPhotoId(String photoId) {
        if (photoId == null || photoId.length() < 2 || photoId.length() > 80) {
            return false;
        }
        for (int index = 0; index < photoId.length(); ++index) {
            boolean asciiLetterOrDigit;
            char character = photoId.charAt(index);
            asciiLetterOrDigit = character >= '0' && character <= '9' || character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z';
            if (asciiLetterOrDigit || character == '_' || character == '-') continue;
            return false;
        }
        return true;
    }
}

