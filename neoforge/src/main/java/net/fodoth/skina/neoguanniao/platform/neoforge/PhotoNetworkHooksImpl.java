package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.network.*;
import net.minecraft.world.InteractionHand;
import java.util.UUID;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;

public final class PhotoNetworkHooksImpl {
    private PhotoNetworkHooksImpl() {}
    public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) { NeoGuanNiao.LOGGER.info("Photo upload begin {} bytes={}", id, bytes); NeoGuanNiaoNetwork.sendToServer(new BeginPhotoUploadPacket(id, hand, bytes, width, height, hash)); }
    public static void uploadChunk(UUID id, int index, byte[] data) { NeoGuanNiaoNetwork.sendToServer(new PhotoUploadChunkPacket(id, index, data)); }
    public static void finishUpload(UUID id) { NeoGuanNiao.LOGGER.info("Photo upload finish {}", id); NeoGuanNiaoNetwork.sendToServer(new FinishPhotoUploadPacket(id)); }
    public static void requestPhoto(String id, String hash) { NeoGuanNiao.LOGGER.info("Photo request send id={}", id); NeoGuanNiaoNetwork.sendToServer(new PhotoRequestPacket(id, hash)); }
}
