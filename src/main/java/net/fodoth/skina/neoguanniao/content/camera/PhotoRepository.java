package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.content.camera.PhotoImageCodec;
import net.fodoth.skina.neoguanniao.content.camera.PhotoTransferLimits;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class PhotoRepository {
    private PhotoRepository() {
    }

    public static void store(MinecraftServer server, String photoId, byte[] jpeg) throws IOException {
        PhotoImageCodec.validateJpeg(jpeg);
        PhotoRepository.storeValidated(server, photoId, jpeg);
    }

    public static void storeValidated(MinecraftServer server, String photoId, byte[] jpeg) throws IOException {
        if (jpeg == null || jpeg.length <= 0 || jpeg.length > net.fodoth.skina.neoguanniao.config.CameraConfig.maxCompressedBytes()) {
            throw new IOException("Invalid compressed photograph size");
        }
        Path target = PhotoRepository.photoPath(server, photoId);
        PhotoRepository.writeAtomically(target, jpeg);
    }

    public static byte[] load(MinecraftServer server, String photoId) throws IOException {
        Path target = PhotoRepository.photoPath(server, photoId);
        if (!Files.isRegularFile(target, new LinkOption[0])) {
            throw new IOException("Photograph does not exist");
        }
        long size = Files.size(target);
        if (size <= 0L || size > net.fodoth.skina.neoguanniao.config.CameraConfig.maxCompressedBytes()) {
            throw new IOException("Photograph file has an invalid size");
        }
        byte[] data = Files.readAllBytes(target);
        if (data.length < 4 || (data[0] & 0xFF) != 255 || (data[1] & 0xFF) != 216 || (data[data.length - 2] & 0xFF) != 255 || (data[data.length - 1] & 0xFF) != 217) {
            throw new IOException("Photograph file is not JPEG");
        }
        return data;
    }

    public static boolean exists(MinecraftServer server, String photoId) {
        try {
            return Files.isRegularFile(PhotoRepository.photoPath(server, photoId), new LinkOption[0]);
        }
        catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void moveToTrash(MinecraftServer server, String photoId) throws IOException {
        Path source = PhotoRepository.photoPath(server, photoId);
        if (!Files.isRegularFile(source, new LinkOption[0])) {
            throw new IOException("Photograph does not exist");
        }
        Path target = PhotoRepository.trashPath(server, photoId);
        Files.createDirectories(target.getParent(), new FileAttribute[0]);
        PhotoRepository.moveAtomically(source, target);
    }

    public static void restoreFromTrash(MinecraftServer server, String photoId) throws IOException {
        Path source = PhotoRepository.trashPath(server, photoId);
        if (!Files.isRegularFile(source, new LinkOption[0])) {
            throw new IOException("Photograph is not in the trash");
        }
        Path target = PhotoRepository.photoPath(server, photoId);
        Files.createDirectories(target.getParent(), new FileAttribute[0]);
        PhotoRepository.moveAtomically(source, target);
    }

    public static boolean deletePermanently(MinecraftServer server, String photoId) throws IOException {
        PhotoRepository.validatePhotoId(photoId);
        boolean deleted = Files.deleteIfExists(PhotoRepository.trashPath(server, photoId));
        return Files.deleteIfExists(PhotoRepository.photoPath(server, photoId)) || deleted;
    }

    public static boolean deleteTrashPermanently(MinecraftServer server, String photoId) throws IOException {
        PhotoRepository.validatePhotoId(photoId);
        return Files.deleteIfExists(PhotoRepository.trashPath(server, photoId));
    }

    public static List<String> listStoredPhotoIds(MinecraftServer server, int limit) throws IOException {
        int boundedLimit = Math.max(1, limit);
        Path root = PhotoRepository.root(server);
        if (!Files.isDirectory(root, new LinkOption[0])) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(root, 2, new FileVisitOption[0]);){
            paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.getFileName().toString().endsWith(".jpg")).filter(path -> !path.startsWith(root.resolve("trash"))).limit(boundedLimit).forEach(path -> {
                String name = path.getFileName().toString();
                String id = name.substring(0, name.length() - 4);
                if (PhotoRepository.isValidPhotoId(id)) {
                    ids.add(id);
                }
            });
        }
        return ids;
    }

    static List<String> listStoredPhotoIdsInShards(MinecraftServer server, int firstShard, int shardCount, int limit) throws IOException {
        int boundedLimit = Math.max(1, limit);
        int boundedShardCount = Math.max(1, Math.min(256, shardCount));
        Path root = PhotoRepository.root(server);
        if (!Files.isDirectory(root, new LinkOption[0])) {
            return List.of();
        }
        ArrayList<String> ids = new ArrayList<String>();
        for (int offset = 0; offset < boundedShardCount && ids.size() < boundedLimit; ++offset) {
            int shard = Math.floorMod(firstShard + offset, 256);
            Path directory = root.resolve(String.format(Locale.ROOT, "%02x", shard));
            if (!Files.isDirectory(directory, new LinkOption[0])) continue;
            try (Stream<Path> paths = Files.list(directory);){
                paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.getFileName().toString().endsWith(".jpg")).limit(boundedLimit - ids.size()).forEach(path -> {
                    String name = path.getFileName().toString();
                    String id = name.substring(0, name.length() - 4);
                    if (PhotoRepository.isValidPhotoId(id)) {
                        ids.add(id);
                    }
                });
                continue;
            }
        }
        return ids;
    }

    public static void backupLegacy(MinecraftServer server, String photoId, int[] pixels) throws IOException {
        Path target = PhotoRepository.legacyPath(server, photoId);
        if (Files.exists(target, new LinkOption[0])) {
            return;
        }
        Files.createDirectories(target.getParent(), new FileAttribute[0]);
        Path temporary = target.resolveSibling(String.valueOf(target.getFileName()) + ".tmp");
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))));){
            output.writeInt(256);
            output.writeInt(256);
            output.writeInt(pixels.length);
            for (int pixel : pixels) {
                output.writeInt(pixel);
            }
        }
        PhotoRepository.moveAtomically(temporary, target);
    }

    public static boolean isValidPhotoId(String photoId) {
        return PhotoTransferLimits.isValidPhotoId(photoId);
    }

    static Path photoPath(MinecraftServer server, String photoId) {
        PhotoRepository.validatePhotoId(photoId);
        return PhotoRepository.root(server).resolve(photoId.substring(0, 2).toLowerCase()).resolve(photoId + ".jpg");
    }

    static Path trashPath(MinecraftServer server, String photoId) {
        PhotoRepository.validatePhotoId(photoId);
        return PhotoRepository.root(server).resolve("trash").resolve(photoId.substring(0, 2).toLowerCase()).resolve(photoId + ".jpg");
    }

    private static Path legacyPath(MinecraftServer server, String photoId) {
        PhotoRepository.validatePhotoId(photoId);
        return PhotoRepository.root(server).resolve("legacy").resolve(photoId + ".legacy.gz");
    }

    static Path root(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("neoguanniao").resolve("photos");
    }

    private static void validatePhotoId(String photoId) {
        if (!PhotoRepository.isValidPhotoId(photoId)) {
            throw new IllegalArgumentException("Invalid photograph id");
        }
    }

    private static void writeAtomically(Path target, byte[] data) throws IOException {
        Files.createDirectories(target.getParent(), new FileAttribute[0]);
        Path temporary = target.resolveSibling(String.valueOf(target.getFileName()) + ".tmp");
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);){
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
        PhotoRepository.moveAtomically(temporary, target);
    }

    private static void moveAtomically(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

