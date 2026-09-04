package net.fodoth.skina.neoguanniao.content.camera;
import net.fodoth.skina.neoguanniao.config.CameraConfig;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public final class PhotoImageCodec {
    private static final byte[] PNG_SIGNATURE = new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private PhotoImageCodec() {
    }

    public static byte[] encodePng(int[] nativeAbgrPixels, int width, int height) throws IOException {
        if (!PhotoTransferLimits.isSupportedDimensions(width, height) || nativeAbgrPixels.length != width * height) {
            throw new IOException("Invalid photograph dimensions");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int abgr = nativeAbgrPixels[y * width + x];
                int rgb = (abgr & 0xFF) << 16 | (abgr >>> 8 & 0xFF) << 8 | abgr >>> 16 & 0xFF;
                image.setRGB(x, y, rgb);
            }
        }
        return PhotoImageCodec.encodePng(image);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Dimensions validatePng(byte[] encoded) throws IOException {
        if (encoded.length == 0 || encoded.length > CameraConfig.maxCompressedBytes() || !PhotoImageCodec.isPngSignature(encoded)) {
            throw new IOException("Invalid compressed photograph size");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(encoded));){
            if (input == null) {
                throw new IOException("Unable to read photograph data");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("Unsupported photograph format");
            }
            ImageReader reader = readers.next();
            try {
                int height;
                reader.setInput(input, true, true);
                if (!"png".equalsIgnoreCase(reader.getFormatName())) {
                    throw new IOException("Photograph must be PNG");
                }
                int width = reader.getWidth(0);
                if (!PhotoTransferLimits.isSupportedDimensions(width, height = reader.getHeight(0))) {
                    throw new IOException("Invalid photograph dimensions");
                }
                Dimensions dimensions = new Dimensions(width, height);
                reader.dispose();
                return dimensions;
            }
            catch (Throwable throwable) {
                reader.dispose();
                throw throwable;
            }
        }
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static boolean isSha256(String value) {
        return value != null && value.length() == 64 && value.matches("[0-9a-f]{64}");
    }

    private static byte[] encodePng(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new IOException("No PNG writer is available");
            }
            return output.toByteArray();
        }
    }

    private static boolean isPngSignature(byte[] data) {
        if (data.length < PNG_SIGNATURE.length) {
            return false;
        }
        for (int index = 0; index < PNG_SIGNATURE.length; ++index) {
            if (data[index] != PNG_SIGNATURE[index]) {
                return false;
            }
        }
        return true;
    }

    public record Dimensions(int width, int height) {
    }
}
