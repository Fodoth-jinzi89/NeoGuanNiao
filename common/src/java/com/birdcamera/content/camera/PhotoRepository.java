package com.birdcamera.content.camera;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * 照片文件仓库：在存档内按 shard 分桶存储 JPEG（迁移自 guaniao-2.1.3）。
 */
public final class PhotoRepository {
    private PhotoRepository() {
    }

    public static void store(MinecraftServer server, String photoId, byte[] jpeg) throws IOException {
        PhotoImageCodec.validateJpeg(jpeg);
        storeValidated(server, photoId, jpeg);
    }

    public static void storeValidated(MinecraftServer server, String photoId, byte[] jpeg) throws IOException {
        if (jpeg != null && jpeg.length > 0 && jpeg.length <= PhotoTransferLimits.MAX_COMPRESSED_BYTES) {
            writeAtomically(photoPath(server, photoId), jpeg);
        } else {
            throw new IOException("Invalid compressed photograph size");
        }
    }

    public static byte[] load(MinecraftServer server, String photoId) throws IOException {
        Path target = photoPath(server, photoId);
        if (!Files.isRegularFile(target)) {
            throw new IOException("Photograph does not exist");
        }
        long size = Files.size(target);
        if (size > 0L && size <= PhotoTransferLimits.MAX_COMPRESSED_BYTES) {
            byte[] data = Files.readAllBytes(target);
            if (data.length >= 4
                    && (data[0] & 255) == 255
                    && (data[1] & 255) == 216
                    && (data[data.length - 2] & 255) == 255
                    && (data[data.length - 1] & 255) == 217) {
                return data;
            } else {
                throw new IOException("Photograph file is not JPEG");
            }
        } else {
            throw new IOException("Photograph file has an invalid size");
        }
    }

    public static boolean exists(MinecraftServer server, String photoId) {
        try {
            return Files.isRegularFile(photoPath(server, photoId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void moveToTrash(MinecraftServer server, String photoId) throws IOException {
        Path source = photoPath(server, photoId);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Photograph does not exist");
        }
        Path target = trashPath(server, photoId);
        Files.createDirectories(target.getParent());
        moveAtomically(source, target);
    }

    public static void restoreFromTrash(MinecraftServer server, String photoId) throws IOException {
        Path source = trashPath(server, photoId);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Photograph is not in the trash");
        }
        Path target = photoPath(server, photoId);
        Files.createDirectories(target.getParent());
        moveAtomically(source, target);
    }

    public static boolean deletePermanently(MinecraftServer server, String photoId) throws IOException {
        validatePhotoId(photoId);
        boolean deleted = Files.deleteIfExists(trashPath(server, photoId));
        return Files.deleteIfExists(photoPath(server, photoId)) || deleted;
    }

    public static boolean deleteTrashPermanently(MinecraftServer server, String photoId) throws IOException {
        validatePhotoId(photoId);
        return Files.deleteIfExists(trashPath(server, photoId));
    }

    public static List<String> listStoredPhotoIds(MinecraftServer server, int limit) throws IOException {
        int boundedLimit = Math.max(1, limit);
        Path root = root(server);
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root, 2)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jpg"))
                    .filter(path -> !path.startsWith(root.resolve("trash")))
                    .limit(boundedLimit)
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        String id = name.substring(0, name.length() - 4);
                        if (isValidPhotoId(id)) {
                            ids.add(id);
                        }
                    });
        }
        return ids;
    }

    static List<String> listStoredPhotoIdsInShards(MinecraftServer server, int firstShard, int shardCount, int limit) throws IOException {
        int boundedLimit = Math.max(1, limit);
        int boundedShardCount = Math.max(1, Math.min(256, shardCount));
        Path root = root(server);
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (int offset = 0; offset < boundedShardCount && ids.size() < boundedLimit; offset++) {
            int shard = Math.floorMod(firstShard + offset, 256);
            Path directory = root.resolve(String.format(Locale.ROOT, "%02x", shard));
            if (Files.isDirectory(directory)) {
                try (Stream<Path> paths = Files.list(directory)) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".jpg"))
                            .limit(boundedLimit - ids.size())
                            .forEach(path -> {
                                String name = path.getFileName().toString();
                                String id = name.substring(0, name.length() - 4);
                                if (isValidPhotoId(id)) {
                                    ids.add(id);
                                }
                            });
                }
            }
        }
        return ids;
    }

    static void backupLegacy(MinecraftServer server, String photoId, int[] pixels) throws IOException {
        Path target = legacyPath(server, photoId);
        if (!Files.exists(target)) {
            Files.createDirectories(target.getParent());
            Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
            try (java.io.DataOutputStream output = new java.io.DataOutputStream(
                    new java.io.BufferedOutputStream(new java.util.zip.GZIPOutputStream(
                            Files.newOutputStream(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))))) {
                output.writeInt(256);
                output.writeInt(256);
                output.writeInt(pixels.length);
                for (int pixel : pixels) {
                    output.writeInt(pixel);
                }
            }
            moveAtomically(temporary, target);
        }
    }

    public static boolean isValidPhotoId(String photoId) {
        return PhotoTransferLimits.isValidPhotoId(photoId);
    }

    static Path photoPath(MinecraftServer server, String photoId) {
        validatePhotoId(photoId);
        return root(server).resolve(photoId.substring(0, 2).toLowerCase(Locale.ROOT)).resolve(photoId + ".jpg");
    }

    static Path trashPath(MinecraftServer server, String photoId) {
        validatePhotoId(photoId);
        return root(server).resolve("trash").resolve(photoId.substring(0, 2).toLowerCase(Locale.ROOT)).resolve(photoId + ".jpg");
    }

    private static Path legacyPath(MinecraftServer server, String photoId) {
        validatePhotoId(photoId);
        return root(server).resolve("legacy").resolve(photoId + ".legacy.gz");
    }

    static Path root(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("birdcamera").resolve("photos");
    }

    private static void validatePhotoId(String photoId) {
        if (!isValidPhotoId(photoId)) {
            throw new IllegalArgumentException("Invalid photograph id");
        }
    }

    private static void writeAtomically(Path target, byte[] data) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (FileChannel channel = FileChannel.open(temporary,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
        moveAtomically(temporary, target);
    }

    private static void moveAtomically(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}