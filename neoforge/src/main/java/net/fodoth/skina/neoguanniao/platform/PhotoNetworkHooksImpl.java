package net.fodoth.skina.neoguanniao.platform;

import net.fodoth.skina.neoguanniao.network.*;
import net.minecraft.world.InteractionHand;
import java.util.UUID;

public final class PhotoNetworkHooksImpl {
    private PhotoNetworkHooksImpl() {}
    public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) { NeoGuanNiaoNetwork.sendToServer(new BeginPhotoUploadPacket(id, hand, bytes, width, height, hash)); }
    public static void uploadChunk(UUID id, int index, byte[] data) { NeoGuanNiaoNetwork.sendToServer(new PhotoUploadChunkPacket(id, index, data)); }
    public static void finishUpload(UUID id) { NeoGuanNiaoNetwork.sendToServer(new FinishPhotoUploadPacket(id)); }
    public static void requestPhoto(String id, String hash) { NeoGuanNiaoNetwork.sendToServer(new PhotoRequestPacket(id, hash)); }
}
