package net.fodoth.skina.neoguanniao.client.camera;
import net.fodoth.skina.neoguanniao.content.camera.PhotoImageCodec;
import net.fodoth.skina.neoguanniao.content.camera.PhotoTransferLimits;

import net.fodoth.skina.neoguanniao.network.BeginPhotoUploadPacket;
import net.fodoth.skina.neoguanniao.network.FinishPhotoUploadPacket;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoNetwork;
import net.fodoth.skina.neoguanniao.network.PhotoRequestPacket;
import net.fodoth.skina.neoguanniao.network.PhotoUploadChunkPacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public final class PhotoClientRepository {
        private static final int MAX_CACHED_IMAGES = 192;
    private static final long REQUEST_RETRY_MILLIS = 5000L;
    private static final Map<String, CachedImage> IMAGES = new LinkedHashMap<String, CachedImage>(193, 0.75f, true);
    private static final Map<String, DownloadSession> DOWNLOADS = new HashMap<String, DownloadSession>();
    private static final Map<String, String> EXPECTED_HASHES = new HashMap<String, String>();
    private static final Map<String, Long> RETRY_AFTER = new HashMap<String, Long>();
    private static final Map<UUID, Long> PENDING_UPLOADS = new HashMap<UUID, Long>();
    private static final Map<String, String> REQUEST_QUEUE = new LinkedHashMap<String, String>();
    private static final Map<String, Integer> FAILURE_COUNTS = new HashMap<String, Integer>();
    private static final Set<String> VALIDATING = new HashSet<String>();
    private static final ExecutorService VALIDATION_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Guaniao-Photo-Decode");
        thread.setDaemon(true);
        return thread;
    });
    private static int generation;
    private static String activeRequest;
    private static long activeRequestStarted;

    private PhotoClientRepository() {
    }

    public static void upload(InteractionHand hand, byte[] jpeg) throws IOException {
        PhotoImageCodec.Dimensions dimensions = PhotoImageCodec.validateJpeg(jpeg);
        long now = System.currentTimeMillis();
        if (activeRequest != null && now - activeRequestStarted > 10000L) {
            PhotoClientRepository.reject(activeRequest);
        }
        PENDING_UPLOADS.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 60000L);
        UUID uploadId = UUID.randomUUID();
        String hash = PhotoImageCodec.sha256(jpeg);
        PENDING_UPLOADS.put(uploadId, now);
        NeoGuanNiaoNetwork.sendToServer(new BeginPhotoUploadPacket(uploadId, hand, jpeg.length, dimensions.width(), dimensions.height(), hash));
        int offset = 0;
        int index = 0;
        while (offset < jpeg.length) {
            int end = Math.min(offset + 24576, jpeg.length);
            NeoGuanNiaoNetwork.sendToServer(new PhotoUploadChunkPacket(uploadId, index, Arrays.copyOfRange(jpeg, offset, end)));
            offset += 24576;
            ++index;
        }
        NeoGuanNiaoNetwork.sendToServer(new FinishPhotoUploadPacket(uploadId));
    }

    public static void captureResult(UUID uploadId, boolean success) {
        if (PENDING_UPLOADS.remove(uploadId) == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage((Component)Component.translatable((String)(success ? "item.neoguanniao.nikon_d750.captured" : "item.neoguanniao.nikon_d750.capture_failed")), true);
        }
    }

    public static byte[] getOrRequest(String photoId, String expectedHash) {
        CachedImage cached = IMAGES.get(photoId);
        if (cached != null && (expectedHash == null || expectedHash.isEmpty() || expectedHash.equals(cached.contentHash()))) {
            return cached.data();
        }
        if (cached != null) {
            IMAGES.remove(photoId);
        }
        long now = System.currentTimeMillis();
        if (!(DOWNLOADS.containsKey(photoId) || VALIDATING.contains(photoId) || photoId.equals(activeRequest) || now < RETRY_AFTER.getOrDefault(photoId, 0L))) {
            EXPECTED_HASHES.put(photoId, expectedHash == null ? "" : expectedHash);
            RETRY_AFTER.put(photoId, now + 5000L);
            if (REQUEST_QUEUE.size() < MAX_CACHED_IMAGES) {
                REQUEST_QUEUE.putIfAbsent(photoId, expectedHash == null ? "" : expectedHash);
                PhotoClientRepository.dispatchNextRequest();
            }
        }
        return null;
    }

    public static byte[] cached(String photoId) {
        CachedImage cached = IMAGES.get(photoId);
        return cached == null ? null : cached.data();
    }

    public static void beginDownload(String photoId, boolean found, int totalBytes, int width, int height, String contentHash) {
        if (!found) {
            DOWNLOADS.remove(photoId);
            RETRY_AFTER.put(photoId, System.currentTimeMillis() + 5000L);
            PhotoClientRepository.finishActiveRequest(photoId);
            return;
        }
        String expectedHash = EXPECTED_HASHES.getOrDefault(photoId, "");
        if (totalBytes <= 0 || totalBytes > 0x200000 || !PhotoTransferLimits.isSupportedDimensions(width, height) || !PhotoImageCodec.isSha256(contentHash) || !expectedHash.isEmpty() && !expectedHash.equals(contentHash)) {
            PhotoClientRepository.reject(photoId);
            return;
        }
        DOWNLOADS.put(photoId, new DownloadSession(totalBytes, width, height, contentHash));
    }

    public static void acceptDownloadChunk(String photoId, int chunkIndex, byte[] data) {
        byte[] jpeg;
        DownloadSession session = DOWNLOADS.get(photoId);
        if (session == null || !session.accept(chunkIndex, data)) {
            PhotoClientRepository.reject(photoId);
            return;
        }
        if (!session.complete()) {
            return;
        }
        DOWNLOADS.remove(photoId);
        PhotoClientRepository.finishActiveRequest(photoId);
        try {
            jpeg = session.assemble();
        }
        catch (IOException exception) {
            PhotoClientRepository.reject(photoId);
            return;
        }
        int validationGeneration = generation;
        VALIDATING.add(photoId);
        VALIDATION_EXECUTOR.execute(() -> {
            boolean valid;
            try {
                valid = session.contentHash.equals(PhotoImageCodec.sha256(jpeg));
                if (valid) {
                    PhotoImageCodec.Dimensions dimensions = PhotoImageCodec.validateJpeg(jpeg);
                    valid = dimensions.width() == session.width && dimensions.height() == session.height;
                }
            }
            catch (IOException | RuntimeException exception) {
                valid = false;
            }
            boolean accepted = valid;
            Minecraft.getInstance().execute(() -> PhotoClientRepository.finishValidation(photoId, session.contentHash, jpeg, validationGeneration, accepted));
        });
    }

    public static void clear() {
        IMAGES.clear();
        DOWNLOADS.clear();
        EXPECTED_HASHES.clear();
        RETRY_AFTER.clear();
        PENDING_UPLOADS.clear();
        REQUEST_QUEUE.clear();
        FAILURE_COUNTS.clear();
        VALIDATING.clear();
        ++generation;
        activeRequest = null;
        activeRequestStarted = 0L;
    }

    private static void reject(String photoId) {
        DOWNLOADS.remove(photoId);
        VALIDATING.remove(photoId);
        EXPECTED_HASHES.remove(photoId);
        int failures = Math.min(5, FAILURE_COUNTS.getOrDefault(photoId, 0) + 1);
        FAILURE_COUNTS.put(photoId, failures);
        long delay = Math.min(60000L, REQUEST_RETRY_MILLIS << failures - 1);
        RETRY_AFTER.put(photoId, System.currentTimeMillis() + delay);
        REQUEST_QUEUE.remove(photoId);
        PhotoClientRepository.finishActiveRequest(photoId);
    }

    private static void dispatchNextRequest() {
        if (activeRequest != null || REQUEST_QUEUE.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, String>> iterator = REQUEST_QUEUE.entrySet().iterator();
        Map.Entry<String, String> next = iterator.next();
        activeRequest = next.getKey();
        activeRequestStarted = System.currentTimeMillis();
        String expectedHash = next.getValue();
        iterator.remove();
        NeoGuanNiaoNetwork.sendToServer(new PhotoRequestPacket(activeRequest, expectedHash));
    }

    private static void finishActiveRequest(String photoId) {
        if (photoId.equals(activeRequest)) {
            activeRequest = null;
            activeRequestStarted = 0L;
            PhotoClientRepository.dispatchNextRequest();
        }
    }

    private static void finishValidation(String photoId, String contentHash, byte[] jpeg, int validationGeneration, boolean valid) {
        VALIDATING.remove(photoId);
        if (validationGeneration != generation) {
            return;
        }
        if (!valid) {
            PhotoClientRepository.reject(photoId);
            return;
        }
        IMAGES.put(photoId, new CachedImage(jpeg, contentHash));
        EXPECTED_HASHES.remove(photoId);
        RETRY_AFTER.remove(photoId);
        FAILURE_COUNTS.remove(photoId);
        PhotoClientRepository.evictOldestImages();
    }

    private static void evictOldestImages() {
        Iterator<String> iterator = IMAGES.keySet().iterator();
        while (IMAGES.size() > MAX_CACHED_IMAGES && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    private record CachedImage(byte[] data, String contentHash) {
    }

    private static final class DownloadSession {
        private final int totalBytes;
        private final int width;
        private final int height;
        private final String contentHash;
        private final byte[][] chunks;
        private int receivedBytes;

        private DownloadSession(int totalBytes, int width, int height, String contentHash) {
            this.totalBytes = totalBytes;
            this.width = width;
            this.height = height;
            this.contentHash = contentHash;
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
}

