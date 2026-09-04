package net.fodoth.skina.neoguanniao.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;

/** Registers and routes every camera payload through one typed NeoForge channel. */
@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class NeoGuanNiaoNetwork {

    private NeoGuanNiaoNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("camera-1");
        registrar.playToServer(BeginPhotoUploadPacket.TYPE, BeginPhotoUploadPacket.STREAM_CODEC, BeginPhotoUploadPacket::handle);
        registrar.playToServer(PhotoUploadChunkPacket.TYPE, PhotoUploadChunkPacket.STREAM_CODEC, PhotoUploadChunkPacket::handle);
        registrar.playToServer(FinishPhotoUploadPacket.TYPE, FinishPhotoUploadPacket.STREAM_CODEC, FinishPhotoUploadPacket::handle);
        registrar.playToServer(PhotoRequestPacket.TYPE, PhotoRequestPacket.STREAM_CODEC, PhotoRequestPacket::handle);
        registrar.playToServer(SetCameraFilterPacket.TYPE, SetCameraFilterPacket.STREAM_CODEC, SetCameraFilterPacket::handle);
        registrar.playToServer(SetCameraSettingsPacket.TYPE, SetCameraSettingsPacket.STREAM_CODEC, SetCameraSettingsPacket::handle);
        registrar.playToClient(PhotoCaptureResultPacket.TYPE, PhotoCaptureResultPacket.STREAM_CODEC, PhotoCaptureResultPacket::handle);
        registrar.playToClient(PhotoDownloadStartPacket.TYPE, PhotoDownloadStartPacket.STREAM_CODEC, PhotoDownloadStartPacket::handle);
        registrar.playToClient(PhotoDownloadChunkPacket.TYPE, PhotoDownloadChunkPacket.STREAM_CODEC, PhotoDownloadChunkPacket::handle);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
