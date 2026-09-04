package net.fodoth.skina.neoguanniao.network;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.config.CameraConfig;
import net.fodoth.skina.neoguanniao.content.camera.PhotoImageCodec;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIndexSavedData;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIoService;
import net.fodoth.skina.neoguanniao.content.camera.PhotoRepository;
import net.fodoth.skina.neoguanniao.content.camera.PhotoTransferLimits;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class PhotoUploadManager {
    private static final DateTimeFormatter PHOTO_NAME_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<UUID, UploadSession> ACTIVE_UPLOADS = new HashMap<>();
    private static final Map<UUID, UUID> PROCESSING_UPLOADS = new HashMap<>();
    private static final Map<UUID, Integer> PENDING_STORE_BYTES = new HashMap<>();
    private static final Map<UUID, ArrayDeque<RateEntry>> UPLOAD_RATE = new HashMap<>();
    private static final Map<UUID, RequestWindow> DOWNLOAD_RATE = new HashMap<>();
    private static final Set<UUID> PENDING_DOWNLOADS = new HashSet<>();
    private static final Map<UUID, DownloadSession> DOWNLOADS = new LinkedHashMap<>();
    private static final ArrayDeque<UUID> DOWNLOAD_ORDER = new ArrayDeque<>();

    private PhotoUploadManager() {
    }

    public static void begin(ServerPlayer player, UUID uploadId, InteractionHand hand, int totalBytes, int width, int height, String contentHash) {
        long now = PhotoUploadManager.gameTime(player.server);
        PhotoUploadManager.cleanupExpired(player.server, now);
        if (!PhotoUploadManager.canUpload(player) || ACTIVE_UPLOADS.containsKey(player.getUUID()) || PROCESSING_UPLOADS.containsKey(player.getUUID()) || totalBytes <= 0 || totalBytes > CameraConfig.maxCompressedBytes() || !PhotoTransferLimits.isCaptureDimensions(width, height) || !PhotoImageCodec.isSha256(contentHash) || !PhotoUploadManager.quotaAllows(player.server, player.getUUID(), totalBytes)) {
            PhotoUploadManager.fail(player, uploadId);
            return;
        }
        ItemStack camera = player.getItemInHand(hand);
        if (!camera.is((Item) NeoGuanNiaoItems.NIKON_D750.get()) || player.getCooldowns().isOnCooldown(camera.getItem())) {
            PhotoUploadManager.fail(player, uploadId);
            return;
        }
        if (!PhotoUploadManager.reserveUploadBytes(player.getUUID(), now, totalBytes)) {
            PhotoUploadManager.fail(player, uploadId);
            return;
        }
        player.getCooldowns().addCooldown(camera.getItem(), CameraConfig.captureCooldownTicks());
        ACTIVE_UPLOADS.put(player.getUUID(), new UploadSession(uploadId, totalBytes, contentHash, now));
    }

    public static void acceptChunk(ServerPlayer player, UUID uploadId, int chunkIndex, byte[] data) {
        UploadSession session = ACTIVE_UPLOADS.get(player.getUUID());
        if (session == null) {
            return;
        }
        if (!session.uploadId.equals(uploadId) || !session.accept(chunkIndex, data)) {
            ACTIVE_UPLOADS.remove(player.getUUID());
            PhotoUploadManager.fail(player, session.uploadId);
        }
    }

    public static void finish(ServerPlayer player, UUID uploadId) {
        UUID playerId = player.getUUID();
        UploadSession session = ACTIVE_UPLOADS.remove(playerId);
        if (session == null) {
            return;
        }
        if (!(session.uploadId.equals(uploadId) && session.complete() && PhotoUploadManager.quotaAllows(player.server, playerId, session.totalBytes))) {
            PhotoUploadManager.fail(player, session.uploadId);
            return;
        }
        MinecraftServer server = player.server;
        String ownerName = player.getScoreboardName();
        long gameTime = player.level().getGameTime();
        String filmName = PhotoUploadManager.captureDate();
        String location = PhotoUploadManager.captureLocation(player);
        PhotoUploadManager.reservePendingStore(playerId, session.totalBytes);
        PROCESSING_UPLOADS.put(playerId, uploadId);
        boolean accepted = PhotoIoService.submit(server, () -> PhotoUploadManager.validateAndStore(server, session), result -> PhotoUploadManager.finishStoredUpload(server, playerId, ownerName, gameTime, filmName, location, uploadId, result), throwable -> {
            PhotoUploadManager.releasePendingStore(playerId);
            PROCESSING_UPLOADS.remove(playerId);
            NeoGuanNiao.LOGGER.warn("Failed to validate or store photograph for {}", (Object) ownerName, throwable);
            ServerPlayer online = server.getPlayerList().getPlayer(playerId);
            if (online != null) {
                PhotoUploadManager.fail(online, uploadId);
            }
        });
        if (!accepted) {
            PhotoUploadManager.releasePendingStore(playerId);
            PROCESSING_UPLOADS.remove(playerId);
            PhotoUploadManager.fail(player, uploadId);
        }
    }

    public static void requestDownload(ServerPlayer player, String photoId, String expectedHash) {
        UUID playerId = player.getUUID();
        if (!PhotoUploadManager.allowDownload(player) || !PhotoRepository.isValidPhotoId(photoId) || !expectedHash.isEmpty() && !PhotoImageCodec.isSha256(expectedHash) || PENDING_DOWNLOADS.contains(playerId) || DOWNLOADS.containsKey(playerId) || PENDING_DOWNLOADS.size() + DOWNLOADS.size() >= CameraConfig.maxConcurrentDownloads()) {
            PhotoUploadManager.sendMissing(player, photoId);
            return;
        }
        PhotoIndexSavedData.PhotoRecord indexed = PhotoIndexSavedData.get(player.server).get(photoId);
        if (indexed == null) {
            PhotoUploadManager.sendMissing(player, photoId);
            return;
        }
        if (indexed.status() != PhotoIndexSavedData.PhotoStatus.ACTIVE || !expectedHash.isEmpty() && !expectedHash.equals(indexed.contentHash())) {
            PhotoUploadManager.sendMissing(player, photoId);
            return;
        }
        MinecraftServer server = player.server;
        PENDING_DOWNLOADS.add(playerId);
        boolean accepted = PhotoIoService.submit(server, () -> PhotoUploadManager.loadDownload(server, photoId, expectedHash), loaded -> {
            PENDING_DOWNLOADS.remove(playerId);
            ServerPlayer online = server.getPlayerList().getPlayer(playerId);
            if (online == null) {
                return;
            }
            PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
            PhotoIndexSavedData.PhotoRecord record = index.get(photoId);
            long now = System.currentTimeMillis();
            if (record == null) {
                PhotoUploadManager.sendMissing(online, photoId);
                return;
            }
            index.updateFileMetadata(photoId, loaded.data.length, loaded.width, loaded.height, loaded.contentHash, now);
            NeoGuanNiaoNetwork.sendToPlayer(new PhotoDownloadStartPacket(photoId, true, loaded.data.length, loaded.width, loaded.height, loaded.contentHash), online);
            DOWNLOADS.put(playerId, new DownloadSession(photoId, loaded.data, loaded.contentHash, PhotoUploadManager.gameTime(server)));
            DOWNLOAD_ORDER.addLast(playerId);
        }, throwable -> {
            PENDING_DOWNLOADS.remove(playerId);
            ServerPlayer online = server.getPlayerList().getPlayer(playerId);
            if (online != null) {
                NeoGuanNiao.LOGGER.debug("Unable to serve photograph {} to {}", new Object[]{photoId, online.getScoreboardName(), throwable});
                PhotoUploadManager.sendMissing(online, photoId);
            }
        });
        if (!accepted) {
            PENDING_DOWNLOADS.remove(playerId);
            PhotoUploadManager.sendMissing(player, photoId);
        }
    }

    public static boolean allowDownload(ServerPlayer player) {
        long now = PhotoUploadManager.gameTime(player.server);
        RequestWindow window = DOWNLOAD_RATE.computeIfAbsent(player.getUUID(), ignored -> new RequestWindow(now));
        if (now - window.startedAt >= 20L) {
            window.startedAt = now;
            window.requests = 0;
        }
        return ++window.requests <= 8;
    }

    public static void tick(MinecraftServer server) {
        UUID playerId;
        long now = PhotoUploadManager.gameTime(server);
        PhotoUploadManager.cleanupExpired(server, now);
        int budget = CameraConfig.downloadBytesPerTick();
        int sessionsThisTick = DOWNLOAD_ORDER.size();
        for (int visited = 0; visited < sessionsThisTick && budget > 0 && (playerId = DOWNLOAD_ORDER.pollFirst()) != null; ++visited) {
            DownloadSession session = DOWNLOADS.get(playerId);
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (session == null || player == null || now - session.lastProgressAt > CameraConfig.downloadTimeoutTicks()) {
                DOWNLOADS.remove(playerId);
                continue;
            }
            int chunkBytes = session.nextChunkBytes();
            if (chunkBytes > budget) {
                DOWNLOAD_ORDER.addFirst(playerId);
                break;
            }
            NeoGuanNiaoNetwork.sendToPlayer(session.nextPacket(), player);
            budget -= chunkBytes;
            session.lastProgressAt = now;
            if (session.complete()) {
                DOWNLOADS.remove(playerId);
                continue;
            }
            DOWNLOAD_ORDER.addLast(playerId);
        }
    }

    public static void disconnect(UUID playerId) {
        ACTIVE_UPLOADS.remove(playerId);
        DOWNLOAD_RATE.remove(playerId);
        PENDING_DOWNLOADS.remove(playerId);
        DOWNLOADS.remove(playerId);
        DOWNLOAD_ORDER.removeIf(playerId::equals);
    }

    public static void clear() {
        ACTIVE_UPLOADS.clear();
        PROCESSING_UPLOADS.clear();
        PENDING_STORE_BYTES.clear();
        UPLOAD_RATE.clear();
        DOWNLOAD_RATE.clear();
        PENDING_DOWNLOADS.clear();
        DOWNLOADS.clear();
        DOWNLOAD_ORDER.clear();
    }

    public static int activeUploads() {
        return ACTIVE_UPLOADS.size() + PROCESSING_UPLOADS.size();
    }

    public static int activeDownloads() {
        return PENDING_DOWNLOADS.size() + DOWNLOADS.size();
    }

    private static CaptureJobResult validateAndStore(MinecraftServer server, UploadSession session) throws IOException {
        byte[] jpeg = session.assemble();
        String actualHash = PhotoImageCodec.sha256(jpeg);
        if (!session.contentHash.equals(actualHash)) {
            throw new IOException("Photograph hash mismatch");
        }
        PhotoImageCodec.Dimensions dimensions = PhotoImageCodec.validateJpeg(jpeg);
        String photoId = UUID.randomUUID().toString();
        PhotoRepository.storeValidated(server, photoId, jpeg);
        return new CaptureJobResult(photoId, jpeg.length, dimensions.width(), dimensions.height(), actualHash);
    }

    private static void finishStoredUpload(MinecraftServer server, UUID playerId, String ownerName, long gameTime, String filmName, String location, UUID uploadId, CaptureJobResult result) {
        PhotoUploadManager.releasePendingStore(playerId);
        PROCESSING_UPLOADS.remove(playerId);
        long now = System.currentTimeMillis();
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        index.register(new PhotoIndexSavedData.PhotoRecord(result.photoId, playerId, ownerName, now, now, 0L, result.bytes, result.width, result.height, result.contentHash, PhotoIndexSavedData.PhotoStatus.ACTIVE));
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) {
            PhotoUploadManager.trashUndelivered(server, index, result.photoId, now);
            return;
        }
        try {
            ItemStack film = new ItemStack((ItemLike) NeoGuanNiaoItems.FILM.get());
            PhotographData.writeReference(film, result.photoId, ownerName, playerId, gameTime, result.width, result.height, result.contentHash, player.level().dimension().location().toString(), player.getBlockX(), player.getBlockY(), player.getBlockZ());
            if (!player.getInventory().add(film)) {
                player.drop(film, false);
            }
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.8f, 1.25f);
            NeoGuanNiaoNetwork.sendToPlayer(new PhotoCaptureResultPacket(uploadId, true), player);
        } catch (RuntimeException exception) {
            NeoGuanNiao.LOGGER.warn("Failed to deliver stored photograph {}", result.photoId, exception);
            PhotoUploadManager.trashUndelivered(server, index, result.photoId, now);
            PhotoUploadManager.fail(player, uploadId);
        }
    }

    private static void trashUndelivered(MinecraftServer server, PhotoIndexSavedData index, String photoId, long now) {
        index.moveToTrash(photoId, now);
        PhotoIoService.submit(server, () -> {
            PhotoRepository.moveToTrash(server, photoId);
            return photoId;
        }, ignored -> {
        }, throwable -> NeoGuanNiao.LOGGER.warn("Failed to trash an undelivered photograph {}", (Object) photoId, throwable));
    }

    private static LoadedDownload loadDownload(MinecraftServer server, String photoId, String expectedHash) throws IOException {
        byte[] data = PhotoRepository.load(server, photoId);
        String hash = PhotoImageCodec.sha256(data);
        if (!expectedHash.isEmpty() && !expectedHash.equals(hash)) {
            throw new IOException("Photograph hash mismatch");
        }
        PhotoImageCodec.Dimensions dimensions = PhotoImageCodec.validateJpeg(data);
        return new LoadedDownload(data, hash, dimensions.width(), dimensions.height());
    }

    private static boolean canUpload(ServerPlayer player) {
        return CameraConfig.uploadsEnabled()
                && (!CameraConfig.uploadsOperatorOnly() || player.hasPermissions(2))
                && (!CameraConfig.uploadsWhitelistedOnly() || player.hasPermissions(2)
                || player.server.getPlayerList().isWhiteListed(player.getGameProfile()));
    }

    private static boolean quotaAllows(MinecraftServer server, UUID playerId, int bytes) {
        PhotoIndexSavedData.Usage usage = PhotoIndexSavedData.get(server).usage(playerId);
        int pendingWorldCount = PENDING_STORE_BYTES.size();
        long pendingWorldBytes = PENDING_STORE_BYTES.values().stream().mapToLong(Integer::longValue).sum();
        int pendingPlayerCount = PENDING_STORE_BYTES.containsKey(playerId) ? 1 : 0;
        long pendingPlayerBytes = PENDING_STORE_BYTES.getOrDefault(playerId, 0);
        return usage.playerCount() + pendingPlayerCount + 1 <= CameraConfig.maxPhotosPerPlayer()
                && usage.playerBytes() + pendingPlayerBytes + bytes <= CameraConfig.maxPhotoBytesPerPlayer()
                && usage.worldCount() + pendingWorldCount + 1 <= CameraConfig.maxPhotosPerWorld()
                && usage.worldBytes() + pendingWorldBytes + bytes <= CameraConfig.maxPhotoBytesPerWorld();
    }

    private static void reservePendingStore(UUID playerId, int bytes) {
        PENDING_STORE_BYTES.put(playerId, bytes);
    }

    private static void releasePendingStore(UUID playerId) {
        PENDING_STORE_BYTES.remove(playerId);
    }

    private static boolean reserveUploadBytes(UUID playerId, long now, int bytes) {
        ArrayDeque<RateEntry> entries = UPLOAD_RATE.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        while (!entries.isEmpty() && now - ((RateEntry) entries.peekFirst()).gameTime >= 1200L) {
            entries.removeFirst();
        }
        int used = entries.stream().mapToInt(RateEntry::bytes).sum();
        if (used + bytes > CameraConfig.maxUploadBytesPerMinute()) {
            return false;
        }
        entries.addLast(new RateEntry(now, bytes));
        return true;
    }

    private static void cleanupExpired(MinecraftServer server, long now) {
        ACTIVE_UPLOADS.entrySet().removeIf(entry -> {
            UploadSession session = (UploadSession) entry.getValue();
            if (now - session.startedAt <= CameraConfig.uploadTimeoutTicks()) {
                return false;
            }
            ServerPlayer player = server.getPlayerList().getPlayer((UUID) entry.getKey());
            if (player != null) {
                PhotoUploadManager.fail(player, session.uploadId);
            }
            return true;
        });
        UPLOAD_RATE.entrySet().removeIf(entry -> {
            ArrayDeque<RateEntry> entries = entry.getValue();
            while (!entries.isEmpty() && now - ((RateEntry) entries.peekFirst()).gameTime >= 1200L) {
                entries.removeFirst();
            }
            return entries.isEmpty();
        });
    }

    private static void fail(ServerPlayer player, UUID uploadId) {
        NeoGuanNiaoNetwork.sendToPlayer(new PhotoCaptureResultPacket(uploadId, false), player);
    }

    private static void sendMissing(ServerPlayer player, String photoId) {
        NeoGuanNiaoNetwork.sendToPlayer(PhotoDownloadStartPacket.missing(photoId), player);
    }

    private static long gameTime(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    private static String captureDate() {
        return LocalDateTime.now().format(PHOTO_NAME_DATE);
    }

    private static String captureLocation(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        String dimension = player.level().dimension().location().getPath();
        return dimension + " " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static final class UploadSession {
        private final UUID uploadId;
        private final int totalBytes;
        private final String contentHash;
        private final long startedAt;
        private final byte[][] chunks;
        private int receivedBytes;

        private UploadSession(UUID uploadId, int totalBytes, String contentHash, long startedAt) {
            this.uploadId = uploadId;
            this.totalBytes = totalBytes;
            this.contentHash = contentHash;
            this.startedAt = startedAt;
            this.chunks = new byte[(totalBytes + 24576 - 1) / 24576][];
        }

        private boolean accept(int chunkIndex, byte[] data) {
            if (chunkIndex < 0 || chunkIndex >= this.chunks.length || this.chunks[chunkIndex] != null) {
                return false;
            }
            int expected = Math.min(24576, this.totalBytes - chunkIndex * 24576);
            if (data.length != expected || this.receivedBytes + data.length > this.totalBytes) {
                return false;
            }
            this.chunks[chunkIndex] = Arrays.copyOf(data, data.length);
            this.receivedBytes += data.length;
            return true;
        }

        private boolean complete() {
            return this.receivedBytes == this.totalBytes && Arrays.stream(this.chunks).allMatch(Objects::nonNull);
        }

        private byte[] assemble() throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream(this.totalBytes);
            for (byte[] chunk : this.chunks) {
                output.write(chunk);
            }
            return output.toByteArray();
        }
    }

    private static final class RequestWindow {
        private long startedAt;
        private int requests;

        private RequestWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    private static final class DownloadSession {
        private final String photoId;
        private final byte[] data;
        private final String contentHash;
        private long lastProgressAt;
        private int offset;
        private int chunkIndex;

        private DownloadSession(String photoId, byte[] data, String contentHash, long startedAt) {
            this.photoId = photoId;
            this.data = data;
            this.contentHash = contentHash;
            this.lastProgressAt = startedAt;
        }

        private int nextChunkBytes() {
            return Math.min(24576, this.data.length - this.offset);
        }

        private PhotoDownloadChunkPacket nextPacket() {
            int end = Math.min(this.offset + 24576, this.data.length);
            byte[] chunk = Arrays.copyOfRange(this.data, this.offset, end);
            PhotoDownloadChunkPacket packet = new PhotoDownloadChunkPacket(this.photoId, this.chunkIndex++, chunk);
            this.offset = end;
            return packet;
        }

        private boolean complete() {
            return this.offset >= this.data.length;
        }

        public String getContentHash() {
            return contentHash;
        }
    }

    private record CaptureJobResult(String photoId, int bytes, int width, int height, String contentHash) {
    }

    private record LoadedDownload(byte[] data, String contentHash, int width, int height) {
    }

    private record RateEntry(long gameTime, int bytes) {
    }
}

