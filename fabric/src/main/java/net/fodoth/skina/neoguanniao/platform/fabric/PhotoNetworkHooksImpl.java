package net.fodoth.skina.neoguanniao.platform.fabric;

import net.minecraft.world.InteractionHand;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fodoth.skina.neoguanniao.network.*;
import net.fodoth.skina.neoguanniao.network.PhotoUploadManager;

public final class PhotoNetworkHooksImpl {
    private PhotoNetworkHooksImpl() {
    }

    // Photo transfers are not implemented on Fabric yet.
    public static int activeUploads() {
        return PhotoUploadManager.activeUploads();
    }

    public static int activeDownloads() {
        return PhotoUploadManager.activeDownloads();
    }

    public static void beginUpload(UUID id, InteractionHand hand, int bytes, int width, int height, String hash) {
        if (ClientPlayNetworking.canSend(BeginPhotoUploadPacket.TYPE)) {
            ClientPlayNetworking.send(new BeginPhotoUploadPacket(id, hand, bytes, width, height, hash));
        }
    }

    public static void uploadChunk(UUID id, int index, byte[] data) {
        if (ClientPlayNetworking.canSend(PhotoUploadChunkPacket.TYPE)) {
            ClientPlayNetworking.send(new PhotoUploadChunkPacket(id, index, data));
        }
    }

    public static void finishUpload(UUID id) {
        if (ClientPlayNetworking.canSend(FinishPhotoUploadPacket.TYPE)) {
            ClientPlayNetworking.send(new FinishPhotoUploadPacket(id));
        }
    }

    public static void requestPhoto(String id, String hash) {
        if (ClientPlayNetworking.canSend(PhotoRequestPacket.TYPE)) {
            ClientPlayNetworking.send(new PhotoRequestPacket(id, hash));
        }
    }
}
