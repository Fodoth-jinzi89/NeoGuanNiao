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
    private static final float[] JPEG_QUALITIES = new float[]{0.98f, 0.96f, 0.94f, 0.9f};

    private PhotoImageCodec() {
    }

    public static byte[] encodeJpeg(int[] nativeAbgrPixels, int width, int height) throws IOException {
        if (!PhotoTransferLimits.isSupportedDimensions(width, height) || nativeAbgrPixels.length != width * height) {
            throw new IOException("Invalid photograph dimensions");
        }
        BufferedImage image = new BufferedImage(width, height, 1);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int abgr = nativeAbgrPixels[y * width + x];
                int rgb = (abgr & 0xFF) << 16 | (abgr >>> 8 & 0xFF) << 8 | abgr >>> 16 & 0xFF;
                image.setRGB(x, y, rgb);
            }
        }
        for (float quality : JPEG_QUALITIES) {
            byte[] encoded = PhotoImageCodec.encodeJpeg(image, quality);
            if (encoded.length > CameraConfig.maxCompressedBytes()) continue;
            return encoded;
        }
        throw new IOException("Photograph remains too large after JPEG compression");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Dimensions validateJpeg(byte[] encoded) throws IOException {
        if (encoded.length == 0 || encoded.length > CameraConfig.maxCompressedBytes()) {
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
                if (!"JPEG".equalsIgnoreCase(reader.getFormatName())) {
                    throw new IOException("Photograph must be JPEG");
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

    private static byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        var writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer is available");
        }
        var writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             var stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            var params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(Math.clamp(quality, 0.1F, 1.0F));
            }
            writer.write(null, new javax.imageio.IIOImage(image, null, null), params);
            return output.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    public record Dimensions(int width, int height) {
    }
}

