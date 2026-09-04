package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.config.CameraConfig;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIndexSavedData;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIoService;
import net.fodoth.skina.neoguanniao.content.camera.PhotoRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;

public final class PhotoMaintenance {
    private static final int MAX_FILE_ACTIONS = 128;
    private static final int AUTOMATIC_SHARDS_PER_PASS = 16;
    private static final AtomicBoolean RUNNING = new AtomicBoolean();
    private static int nextAutomaticShard;

    private PhotoMaintenance() {
    }

    public static boolean schedule(MinecraftServer server, boolean dryRun, Consumer<Result> callback) {
        return PhotoMaintenance.schedule(server, dryRun, callback, null);
    }

    public static boolean scheduleAutomatic(MinecraftServer server, Consumer<Result> callback) {
        int firstShard = nextAutomaticShard;
        boolean accepted = PhotoMaintenance.schedule(server, true, callback, new ShardWindow(firstShard, 16));
        if (accepted) {
            nextAutomaticShard = Math.floorMod(firstShard + 16, 256);
        }
        return accepted;
    }

    private static boolean schedule(MinecraftServer server, boolean dryRun, Consumer<Result> callback, ShardWindow shardWindow) {
        if (!RUNNING.compareAndSet(false, true)) {
            return false;
        }
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        Collection<PhotoIndexSavedData.PhotoRecord> records = shardWindow == null ? index.snapshot() : index.snapshotInShards(shardWindow.firstShard, shardWindow.shardCount);
        final long inspectionTime = System.currentTimeMillis();
        final int retentionDays = CameraConfig.trashRetentionDays();
        boolean accepted = PhotoIoService.submit(server, () -> inspect(server, records, retentionDays, inspectionTime, dryRun, shardWindow), result -> {
            PhotoMaintenance.applyIndexChanges(server, result, inspectionTime, dryRun);
            RUNNING.set(false);
            callback.accept((Result)result);
        }, throwable -> {
            RUNNING.set(false);
            NeoGuanNiao.LOGGER.warn("Photograph maintenance failed", throwable);
            callback.accept(Result.failed(throwable.getMessage()));
        });
        if (!accepted) {
            RUNNING.set(false);
        }
        return accepted;
    }

    public static boolean isRunning() {
        return RUNNING.get();
    }

    public static void reset() {
        RUNNING.set(false);
        nextAutomaticShard = 0;
    }

    private static Result inspect(MinecraftServer server, Collection<PhotoIndexSavedData.PhotoRecord> records, int retentionDays, long now, boolean dryRun, ShardWindow shardWindow) throws IOException {
        HashMap<String, PhotoIndexSavedData.PhotoRecord> indexed = new HashMap<String, PhotoIndexSavedData.PhotoRecord>();
        for (PhotoIndexSavedData.PhotoRecord record : records) {
            indexed.put(record.id(), record);
        }
        HashSet<String> stored = new HashSet<String>(shardWindow == null ? PhotoRepository.listStoredPhotoIds(server, Integer.MAX_VALUE) : PhotoRepository.listStoredPhotoIdsInShards(server, shardWindow.firstShard, shardWindow.shardCount, Integer.MAX_VALUE));
        ArrayList<String> missing = new ArrayList<String>();
        ArrayList<OrphanFile> orphans = new ArrayList<OrphanFile>();
        ArrayList<String> deletedTrash = new ArrayList<String>();
        int actions = 0;
        for (PhotoIndexSavedData.PhotoRecord record : records) {
            if (shardWindow != null && !shardWindow.includes(record.id())) continue;
            if (record.status() == PhotoIndexSavedData.PhotoStatus.TRASH) {
                long retentionMillis = (long)retentionDays * 86400000L;
                if (record.deletedAt() <= 0L || now - record.deletedAt() < retentionMillis) continue;
                if (dryRun) {
                    deletedTrash.add(record.id());
                    continue;
                }
                if (actions >= 128) continue;
                PhotoRepository.deleteTrashPermanently(server, record.id());
                deletedTrash.add(record.id());
                ++actions;
                continue;
            }
            if (stored.contains(record.id())) continue;
            missing.add(record.id());
        }
        for (String id : stored) {
            if (indexed.containsKey(id)) continue;
            int bytes = (int)Math.min(Integer.MAX_VALUE, Files.size(PhotoRepository.photoPath(server, id)));
            if (dryRun) {
                orphans.add(new OrphanFile(id, bytes));
                continue;
            }
            if (actions >= 128) continue;
            PhotoRepository.moveToTrash(server, id);
            orphans.add(new OrphanFile(id, bytes));
            ++actions;
        }
        return new Result(true, dryRun, stored.size(), missing, orphans, deletedTrash, "");
    }

    private static void applyIndexChanges(MinecraftServer server, Result result, long now, boolean dryRun) {
        if (!result.success() || dryRun) {
            return;
        }
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        for (String id : result.missing()) {
            index.markMissing(id);
        }
        for (OrphanFile orphan : result.orphans()) {
            if (index.get(orphan.id()) != null) continue;
            index.register(new PhotoIndexSavedData.PhotoRecord(orphan.id(), null, "", now, now, now, orphan.bytes(), 0, 0, "", PhotoIndexSavedData.PhotoStatus.TRASH));
        }
        for (String id : result.deletedTrash()) {
            index.remove(id);
        }
    }

    private record ShardWindow(int firstShard, int shardCount) {
        private boolean includes(String photoId) {
            if (photoId == null || photoId.length() < 2) {
                return false;
            }
            try {
                int shard = Integer.parseInt(photoId.substring(0, 2), 16);
                for (int offset = 0; offset < this.shardCount; ++offset) {
                    if (shard != Math.floorMod(this.firstShard + offset, 256)) continue;
                    return true;
                }
            }
            catch (NumberFormatException ignored) {
                return false;
            }
            return false;
        }
    }

    public record OrphanFile(String id, int bytes) {
    }

    public record Result(boolean success, boolean dryRun, int storedFiles, List<String> missing, List<OrphanFile> orphans, List<String> deletedTrash, String error) {
        private static Result failed(String error) {
            return new Result(false, false, 0, List.of(), List.of(), List.of(), error == null ? "unknown" : error);
        }
    }
}

