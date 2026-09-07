package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

public final class PhotoNetworkHooks {
    private PhotoNetworkHooks() {}
    @ExpectPlatform public static int activeUploads() {
        throw new AssertionError();
    }
    @ExpectPlatform public static int activeDownloads() {
        throw new AssertionError();
    }
    @ExpectPlatform public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) {
        throw new AssertionError();
    }
    @ExpectPlatform public static void uploadChunk(UUID id, int index, byte[] data) {
        throw new AssertionError();
    }
    @ExpectPlatform public static void finishUpload(UUID id) {
        throw new AssertionError();
    }
    @ExpectPlatform public static void requestPhoto(String id, String hash) {
        throw new AssertionError();
    }
}
