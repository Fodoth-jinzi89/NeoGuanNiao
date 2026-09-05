package com.birdcamera.content.camera;

import com.birdcamera.BirdCameraMod;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 旧版相片迁移：将 NBT Pixels 迁移到 JPEG 文件仓库（迁移自 guaniao-2.1.3）。
 */
public final class LegacyPhotoMigration {
    private static final Queue<MigrationTask> PENDING = new ArrayDeque<>();
    private static final Set<ItemStack> QUEUED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean processing;

    private LegacyPhotoMigration() {
    }

    public static void queue(Level level, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            observeReference(serverLevel, stack);
            if (PhotographData.hasLegacyPixels(stack)) {
                if (QUEUED.add(stack)) {
                    PENDING.add(new MigrationTask(serverLevel, stack));
                }
            }
        }
    }

    public static void migrateNow(Level level, ItemStack stack) {
        queue(level, stack);
    }

    public static void tick(MinecraftServer server) {
        if (!processing) {
            MigrationTask task = PENDING.poll();
            if (task != null) {
                QUEUED.remove(task.stack());
                if (task.level().getServer() == server && PhotographData.hasLegacyPixels(task.stack())) {
                    String photoId = PhotographData.id(task.stack());
                    if (PhotoRepository.isValidPhotoId(photoId)) {
                        int[] pixels = PhotographData.pixels(task.stack());
                        if (pixels.length == 65536) {
                            int[] snapshot = Arrays.copyOf(pixels, pixels.length);
                            processing = true;
                            boolean accepted = PhotoIoService.submit(server,
                                    () -> migrateFiles(server, photoId, snapshot),
                                    result -> {
                                        processing = false;
                                        if (PhotographData.hasLegacyPixels(task.stack()) && photoId.equals(PhotographData.id(task.stack()))) {
                                            PhotographData.finishLegacyMigration(task.stack(), result.contentHash());
                                        }
                                        registerIndex(server, task.stack(), photoId, result);
                                    },
                                    throwable -> {
                                        processing = false;
                                        BirdCameraMod.LOGGER.warn("Failed to migrate legacy photograph {}", photoId, throwable);
                                    });
                            if (!accepted) {
                                processing = false;
                                if (QUEUED.add(task.stack())) {
                                    PENDING.add(task);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void clear() {
        PENDING.clear();
        QUEUED.clear();
        processing = false;
    }

    private static MigrationResult migrateFiles(MinecraftServer server, String photoId, int[] pixels) throws IOException {
        byte[] jpeg;
        if (!PhotoRepository.exists(server, photoId)) {
            PhotoRepository.backupLegacy(server, photoId, pixels);
            jpeg = PhotoImageCodec.encodeJpeg(pixels, 256, 256);
            PhotoRepository.storeValidated(server, photoId, jpeg);
        } else {
            jpeg = PhotoRepository.load(server, photoId);
            PhotoImageCodec.validateJpeg(jpeg);
        }
        return new MigrationResult(jpeg.length, PhotoImageCodec.sha256(jpeg));
    }

    private static void registerIndex(MinecraftServer server, ItemStack stack, String photoId, MigrationResult result) {
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        if (index.get(photoId) == null) {
            long now = System.currentTimeMillis();
            UUID owner = PhotographData.photographerId(stack);
            index.register(new PhotoIndexSavedData.PhotoRecord(
                    photoId, owner, PhotographData.photographer(stack), now, now, 0L,
                    result.bytes(), 256, 256, result.contentHash(), PhotoIndexSavedData.PhotoStatus.ACTIVE));
        }
    }

    private static void observeReference(ServerLevel level, ItemStack stack) {
        if (PhotographData.hasImage(stack)) {
            String photoId = PhotographData.id(stack);
            PhotoIndexSavedData index = PhotoIndexSavedData.get(level.getServer());
            if (index.get(photoId) == null) {
                String contentHash = PhotographData.contentHash(stack);
                if (PhotoImageCodec.isSha256(contentHash)) {
                    long now = System.currentTimeMillis();
                    index.register(new PhotoIndexSavedData.PhotoRecord(
                            photoId, PhotographData.photographerId(stack), PhotographData.photographer(stack),
                            now, now, 0L, 0, PhotographData.width(stack), PhotographData.height(stack),
                            contentHash, PhotoIndexSavedData.PhotoStatus.ACTIVE));
                }
            }
        }
    }

    private record MigrationResult(int bytes, String contentHash) {
    }

    private record MigrationTask(ServerLevel level, ItemStack stack) {
    }
}