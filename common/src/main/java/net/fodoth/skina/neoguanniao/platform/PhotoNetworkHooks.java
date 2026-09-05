package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

public final class PhotoNetworkHooks {
    private PhotoNetworkHooks() {}
    @ExpectPlatform public static native void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash);
    @ExpectPlatform public static native void uploadChunk(UUID id, int index, byte[] data);
    @ExpectPlatform public static native void finishUpload(UUID id);
    @ExpectPlatform public static native void requestPhoto(String id, String hash);
}
