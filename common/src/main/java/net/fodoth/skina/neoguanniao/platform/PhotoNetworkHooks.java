package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

public final class PhotoNetworkHooks {
    private PhotoNetworkHooks() {}
    @ExpectPlatform public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) {
        invoke("beginUpload", new Class<?>[]{UUID.class, InteractionHand.class, int.class, int.class, int.class, String.class}, id, hand, bytes, width, height, hash);
    }
    @ExpectPlatform public static void uploadChunk(UUID id, int index, byte[] data) {
        invoke("uploadChunk", new Class<?>[]{UUID.class, int.class, byte[].class}, id, index, data);
    }
    @ExpectPlatform public static void finishUpload(UUID id) {
        invoke("finishUpload", new Class<?>[]{UUID.class}, id);
    }
    @ExpectPlatform public static void requestPhoto(String id, String hash) {
        invoke("requestPhoto", new Class<?>[]{String.class, String.class}, id, hash);
    }

    private static void invoke(String method, Class<?>[] types, Object... args) {
        try {
            Class.forName("net.fodoth.skina.neoguanniao.platform.neoforge.PhotoNetworkHooksImpl")
                    .getMethod(method, types).invoke(null, args);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
