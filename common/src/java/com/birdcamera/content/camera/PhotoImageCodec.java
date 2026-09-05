package com.birdcamera.content.camera;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * 照片图像编解码：JPEG 压缩、校验、SHA-256（迁移自 guaniao-2.1.3）。
 */
public final class PhotoImageCodec {
    private static final float[] JPEG_QUALITIES = new float[]{0.75F, 0.65F, 0.55F, 0.45F};

    private PhotoImageCodec() {
    }

    /** NativeImage 的像素为 ABGR 顺序，这里转换为 RGB 后写入 BufferedImage 并编码 JPEG */
    public static byte[] encodeJpeg(int[] nativeAbgrPixels, int width, int height) throws IOException {
        if (width == 256 && height == 256 && nativeAbgrPixels.length == width * height) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int abgr = nativeAbgrPixels[y * width + x];
                    int rgb = ((abgr & 0xFF) << 16) | (((abgr >>> 8) & 0xFF) << 8) | ((abgr >>> 16) & 0xFF);
                    image.setRGB(x, y, rgb);
                }
            }
            for (float quality : JPEG_QUALITIES) {
                byte[] encoded = encodeJpeg(image, quality);
                if (encoded.length <= PhotoTransferLimits.MAX_COMPRESSED_BYTES) {
                    return encoded;
                }
            }
            throw new IOException("Photograph remains too large after JPEG compression");
        } else {
            throw new IOException("Invalid photograph dimensions");
        }
    }

    public static Dimensions validateJpeg(byte[] encoded) throws IOException {
        if (encoded.length > 0 && encoded.length <= PhotoTransferLimits.MAX_COMPRESSED_BYTES) {
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(encoded))) {
                if (input == null) {
                    throw new IOException("Unable to read photograph data");
                }
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) {
                    throw new IOException("Unsupported photograph format");
                }
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input, true, true);
                    if (!"JPEG".equalsIgnoreCase(reader.getFormatName())) {
                        throw new IOException("Photograph must be JPEG");
                    }
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    if (width != 256 || height != 256) {
                        throw new IOException("Invalid photograph dimensions");
                    }
                    if (reader.read(0) == null) {
                        throw new IOException("Unable to decode photograph");
                    }
                    return new Dimensions(width, height);
                } finally {
                    reader.dispose();
                }
            }
        } else {
            throw new IOException("Invalid compressed photograph size");
        }
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public static boolean isSha256(String value) {
        return value != null && value.length() == PhotoTransferLimits.SHA256_HEX_LENGTH && value.matches("[0-9a-f]{64}");
    }

    private static byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("JPEG writer is unavailable");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), parameters);
            imageOutput.flush();
            return output.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    public record Dimensions(int width, int height) {
    }
}