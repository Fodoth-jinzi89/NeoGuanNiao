package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.camera.PhotoRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class PhotoIndexSavedData
extends SavedData {
    private static final String DATA_NAME = "guaniao_photo_index";
    private static final long ACCESS_UPDATE_INTERVAL_MILLIS = 21600000L;
    private final Map<String, PhotoRecord> records = new HashMap<String, PhotoRecord>();
    private final Map<Integer, Set<String>> idsByShard = new HashMap<Integer, Set<String>>();
    private final Map<UUID, OwnerUsage> usageByOwner = new HashMap<UUID, OwnerUsage>();
    private int worldCount;
    private long worldBytes;
    private int activeCount;
    private int trashCount;
    private int missingCount;

    public static PhotoIndexSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PhotoIndexSavedData::new, PhotoIndexSavedData::load, DataFixTypes.SAVED_DATA_MAP_DATA),
                DATA_NAME
        );
    }

    public static PhotoIndexSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PhotoIndexSavedData data = new PhotoIndexSavedData();
        ListTag list = tag.getList("Photos", 10);
        for (int index = 0; index < list.size(); ++index) {
            CompoundTag entry = list.getCompound(index);
            String id = entry.getString("Id");
            if (!PhotoRepository.isValidPhotoId(id)) continue;
            UUID owner = entry.hasUUID("Owner") ? entry.getUUID("Owner") : null;
            PhotoStatus status = PhotoStatus.byName(entry.getString("Status"));
            PhotoRecord record = new PhotoRecord(id, owner, entry.getString("OwnerName"), Math.max(0L, entry.getLong("CreatedAt")), Math.max(0L, entry.getLong("LastAccessAt")), Math.max(0L, entry.getLong("DeletedAt")), Math.max(0, entry.getInt("Bytes")), Math.max(0, entry.getInt("Width")), Math.max(0, entry.getInt("Height")), entry.getString("Hash"), status);
            data.putRecord(record, false);
        }
        return data;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (PhotoRecord record : this.records.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", record.id());
            if (record.owner() != null) {
                entry.putUUID("Owner", record.owner());
            }
            entry.putString("OwnerName", record.ownerName());
            entry.putLong("CreatedAt", record.createdAt());
            entry.putLong("LastAccessAt", record.lastAccessAt());
            entry.putLong("DeletedAt", record.deletedAt());
            entry.putInt("Bytes", record.bytes());
            entry.putInt("Width", record.width());
            entry.putInt("Height", record.height());
            entry.putString("Hash", record.contentHash());
            entry.putString("Status", record.status().serializedName());
            list.add(entry);
        }
        tag.put("Photos", (Tag)list);
        return tag;
    }

    public PhotoRecord get(String photoId) {
        return this.records.get(photoId);
    }

    public Collection<PhotoRecord> snapshot() {
        return Collections.unmodifiableList(new ArrayList<PhotoRecord>(this.records.values()));
    }

    public Collection<PhotoRecord> snapshotInShards(int firstShard, int shardCount) {
        int boundedCount = Math.max(1, Math.min(256, shardCount));
        ArrayList<PhotoRecord> snapshot = new ArrayList<PhotoRecord>();
        for (int offset = 0; offset < boundedCount; ++offset) {
            int shard = Math.floorMod(firstShard + offset, 256);
            Set<String> ids = this.idsByShard.get(shard);
            if (ids == null) continue;
            for (String id : ids) {
                PhotoRecord record = this.records.get(id);
                if (record == null) continue;
                snapshot.add(record);
            }
        }
        return Collections.unmodifiableList(snapshot);
    }

    public void register(PhotoRecord record) {
        this.putRecord(record, true);
    }

    public void touch(String photoId, long now) {
        PhotoRecord record = this.records.get(photoId);
        if (record == null || now <= record.lastAccessAt() || now - record.lastAccessAt() < 21600000L) {
            return;
        }
        this.putRecord(record.withLastAccess(now), true);
    }

    public void updateFileMetadata(String photoId, int bytes, int width, int height, String contentHash, long now) {
        long lastAccess;
        PhotoRecord record = this.records.get(photoId);
        if (record == null) {
            return;
        }
        int normalizedBytes = Math.max(0, bytes);
        int normalizedWidth = Math.max(0, width);
        int normalizedHeight = Math.max(0, height);
        String normalizedHash = contentHash == null ? record.contentHash() : contentHash;
        long l = lastAccess = now - record.lastAccessAt() >= 21600000L ? Math.max(record.lastAccessAt(), now) : record.lastAccessAt();
        if (record.bytes() == normalizedBytes && record.width() == normalizedWidth && record.height() == normalizedHeight && record.lastAccessAt() == lastAccess && Objects.equals(record.contentHash(), normalizedHash)) {
            return;
        }
        this.putRecord(new PhotoRecord(record.id(), record.owner(), record.ownerName(), record.createdAt(), lastAccess, record.deletedAt(), normalizedBytes, normalizedWidth, normalizedHeight, normalizedHash, record.status()), true);
    }

    public boolean moveToTrash(String photoId, long now) {
        PhotoRecord record = this.records.get(photoId);
        if (record == null || record.status() == PhotoStatus.TRASH) {
            return false;
        }
        this.putRecord(record.withStatus(PhotoStatus.TRASH, now), true);
        return true;
    }

    public boolean restore(String photoId) {
        PhotoRecord record = this.records.get(photoId);
        if (record == null || record.status() != PhotoStatus.TRASH) {
            return false;
        }
        this.putRecord(record.withStatus(PhotoStatus.ACTIVE, 0L), true);
        return true;
    }

    public void markMissing(String photoId) {
        PhotoRecord record = this.records.get(photoId);
        if (record != null && record.status() != PhotoStatus.MISSING) {
            this.putRecord(record.withStatus(PhotoStatus.MISSING, 0L), true);
        }
    }

    public void remove(String photoId) {
        PhotoRecord removed = this.records.remove(photoId);
        if (removed != null) {
            this.removeShardId(photoId);
            this.removeUsage(removed);
            this.setDirty();
        }
    }

    public Usage usage(UUID owner) {
        OwnerUsage player = owner == null ? null : this.usageByOwner.get(owner);
        return new Usage(player == null ? 0 : player.count, player == null ? 0L : player.bytes, this.worldCount, this.worldBytes, this.activeCount, this.trashCount, this.missingCount);
    }

    public List<PhotoRecord> ownedBy(UUID owner) {
        if (owner == null) {
            return List.of();
        }
        return this.records.values().stream().filter(record -> owner.equals(record.owner())).toList();
    }

    private void putRecord(PhotoRecord record, boolean markDirty) {
        PhotoRecord previous = this.records.put(record.id(), record);
        if (previous != null) {
            this.removeUsage(previous);
        } else {
            int shard = PhotoIndexSavedData.shardOf(record.id());
            if (shard >= 0) {
                this.idsByShard.computeIfAbsent(shard, ignored -> new HashSet()).add(record.id());
            }
        }
        this.addUsage(record);
        if (markDirty) {
            this.setDirty();
        }
    }

    private void addUsage(PhotoRecord record) {
        ++this.worldCount;
        this.worldBytes += (long)record.bytes();
        if (record.owner() != null) {
            OwnerUsage usage = this.usageByOwner.computeIfAbsent(record.owner(), ignored -> new OwnerUsage());
            ++usage.count;
            usage.bytes += (long)record.bytes();
        }
        switch (record.status()) {
            case ACTIVE: {
                ++this.activeCount;
                break;
            }
            case TRASH: {
                ++this.trashCount;
                break;
            }
            case MISSING: {
                ++this.missingCount;
            }
        }
    }

    private void removeUsage(PhotoRecord record) {
        OwnerUsage usage;
        this.worldCount = Math.max(0, this.worldCount - 1);
        this.worldBytes = Math.max(0L, this.worldBytes - (long)record.bytes());
        if (record.owner() != null && (usage = this.usageByOwner.get(record.owner())) != null) {
            usage.count = Math.max(0, usage.count - 1);
            usage.bytes = Math.max(0L, usage.bytes - (long)record.bytes());
            if (usage.count == 0) {
                this.usageByOwner.remove(record.owner());
            }
        }
        switch (record.status()) {
            case ACTIVE: {
                this.activeCount = Math.max(0, this.activeCount - 1);
                break;
            }
            case TRASH: {
                this.trashCount = Math.max(0, this.trashCount - 1);
                break;
            }
            case MISSING: {
                this.missingCount = Math.max(0, this.missingCount - 1);
            }
        }
    }

    private void removeShardId(String photoId) {
        int shard = PhotoIndexSavedData.shardOf(photoId);
        Set<String> ids = this.idsByShard.get(shard);
        if (ids == null) {
            return;
        }
        ids.remove(photoId);
        if (ids.isEmpty()) {
            this.idsByShard.remove(shard);
        }
    }

    private static int shardOf(String photoId) {
        if (photoId == null || photoId.length() < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(photoId.substring(0, 2), 16);
        }
        catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public static enum PhotoStatus {
        ACTIVE,
        TRASH,
        MISSING;


        private String serializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        private static PhotoStatus byName(String name) {
            for (PhotoStatus status : PhotoStatus.values()) {
                if (!status.serializedName().equalsIgnoreCase(name)) continue;
                return status;
            }
            NeoGuanNiao.LOGGER.warn("Unknown photograph status '{}'; treating as missing", (Object)name);
            return MISSING;
        }
    }

    public record PhotoRecord(String id, UUID owner, String ownerName, long createdAt, long lastAccessAt, long deletedAt, int bytes, int width, int height, String contentHash, PhotoStatus status) {
        public PhotoRecord {
            ownerName = ownerName == null ? "" : ownerName;
            contentHash = contentHash == null ? "" : contentHash;
            status = status == null ? PhotoStatus.ACTIVE : status;
        }

        private PhotoRecord withLastAccess(long value) {
            return new PhotoRecord(this.id, this.owner, this.ownerName, this.createdAt, value, this.deletedAt, this.bytes, this.width, this.height, this.contentHash, this.status);
        }

        private PhotoRecord withStatus(PhotoStatus value, long deleted) {
            return new PhotoRecord(this.id, this.owner, this.ownerName, this.createdAt, this.lastAccessAt, deleted, this.bytes, this.width, this.height, this.contentHash, value);
        }
    }

    private static final class OwnerUsage {
        private int count;
        private long bytes;

        private OwnerUsage() {
        }
    }

    public record Usage(int playerCount, long playerBytes, int worldCount, long worldBytes, int activeCount, int trashCount, int missingCount) {
    }
}

