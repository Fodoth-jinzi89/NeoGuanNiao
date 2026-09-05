package com.birdcamera.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * 自定义网络包注册（Fabric Payload，迁移自 guaniao-2.1.3 的 GuaniaoNetwork）。
 * 仅注册载荷类型与服务端接收器；客户端接收器在客户端入口（BirdCameraClient）中注册。
 */
public final class BirdCameraNetworking {
    private BirdCameraNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BeginPhotoUploadPacket.TYPE, BeginPhotoUploadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PhotoUploadChunkPacket.TYPE, PhotoUploadChunkPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(FinishPhotoUploadPacket.TYPE, FinishPhotoUploadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PhotoRequestPacket.TYPE, PhotoRequestPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoCaptureResultPacket.TYPE, PhotoCaptureResultPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoDownloadStartPacket.TYPE, PhotoDownloadStartPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoDownloadChunkPacket.TYPE, PhotoDownloadChunkPacket.STREAM_CODEC);

        // 客户端 → 服务端
        ServerPlayNetworking.registerGlobalReceiver(BeginPhotoUploadPacket.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            PhotoUploadManager.begin(player, payload.uploadId(), payload.hand(),
                    payload.totalBytes(), payload.width(), payload.height(), payload.contentHash());
        });
        ServerPlayNetworking.registerGlobalReceiver(PhotoUploadChunkPacket.TYPE, (payload, ctx) ->
                PhotoUploadManager.acceptChunk(ctx.player(), payload.uploadId(), payload.chunkIndex(), payload.data()));
        ServerPlayNetworking.registerGlobalReceiver(FinishPhotoUploadPacket.TYPE, (payload, ctx) ->
                PhotoUploadManager.finish(ctx.player(), payload.uploadId()));
        ServerPlayNetworking.registerGlobalReceiver(PhotoRequestPacket.TYPE, (payload, ctx) ->
                PhotoUploadManager.requestDownload(ctx.player(), payload.photoId(), payload.expectedHash()));
    }
}