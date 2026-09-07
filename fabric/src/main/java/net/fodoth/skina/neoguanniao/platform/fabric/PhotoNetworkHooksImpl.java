package net.fodoth.skina.neoguanniao.platform.fabric;
import net.minecraft.world.InteractionHand;
import java.util.UUID;
public final class PhotoNetworkHooksImpl {
    private PhotoNetworkHooksImpl() {}
    // Photo transfers are not implemented on Fabric yet.
    public static int activeUploads() { return 0; }
    public static int activeDownloads() { return 0; }
    public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) {}
    public static void uploadChunk(UUID id, int index, byte[] data) {}
    public static void finishUpload(UUID id) {}
    public static void requestPhoto(String id, String hash) {}
}
