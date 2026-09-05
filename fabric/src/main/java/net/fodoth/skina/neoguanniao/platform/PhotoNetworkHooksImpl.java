package net.fodoth.skina.neoguanniao.platform;
import net.minecraft.world.InteractionHand;
import java.util.UUID;
public final class PhotoNetworkHooksImpl {
    private PhotoNetworkHooksImpl() {}
    public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) {}
    public static void uploadChunk(UUID id, int index, byte[] data) {}
    public static void finishUpload(UUID id) {}
    public static void requestPhoto(String id, String hash) {}
}
