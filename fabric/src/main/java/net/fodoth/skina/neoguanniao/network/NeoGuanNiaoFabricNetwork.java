package net.fodoth.skina.neoguanniao.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric payload registration for the shared packet handlers.
 */
public final class NeoGuanNiaoFabricNetwork {
    private NeoGuanNiaoFabricNetwork() {
    }

    @SuppressWarnings("resource")
    public static void register() {
        PayloadTypeRegistry.playC2S().register(SetCameraSettingsPacket.TYPE, SetCameraSettingsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetCameraFilterPacket.TYPE, SetCameraFilterPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BeginPhotoUploadPacket.TYPE, BeginPhotoUploadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PhotoUploadChunkPacket.TYPE, PhotoUploadChunkPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(FinishPhotoUploadPacket.TYPE, FinishPhotoUploadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PhotoRequestPacket.TYPE, PhotoRequestPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(FeatherFanPiercePacket.TYPE, FeatherFanPiercePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoCaptureResultPacket.TYPE, PhotoCaptureResultPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoDownloadStartPacket.TYPE, PhotoDownloadStartPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PhotoDownloadChunkPacket.TYPE, PhotoDownloadChunkPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetCameraSettingsPacket.TYPE, (p, c) -> SetCameraSettingsPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(SetCameraFilterPacket.TYPE, (p, c) -> SetCameraFilterPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(BeginPhotoUploadPacket.TYPE, (p, c) -> BeginPhotoUploadPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(PhotoUploadChunkPacket.TYPE, (p, c) -> PhotoUploadChunkPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(FinishPhotoUploadPacket.TYPE, (p, c) -> FinishPhotoUploadPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(PhotoRequestPacket.TYPE, (p, c) -> PhotoRequestPacket.handle(p, context(c.player(), c.server()::execute)));
        ServerPlayNetworking.registerGlobalReceiver(FeatherFanPiercePacket.TYPE, (p, c) -> FeatherFanPiercePacket.handle(p, context(c.player(), c.server()::execute)));
    }

    private static PayloadContext context(Player player, java.util.function.Consumer<Runnable> enqueue) {
        return new PayloadContext() {
            public Player player() {
                return player;
            }

            public void enqueueWork(Runnable work) {
                enqueue.accept(work);
            }
        };
    }
}
