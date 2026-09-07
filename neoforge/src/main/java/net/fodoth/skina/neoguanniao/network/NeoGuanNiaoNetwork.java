package net.fodoth.skina.neoguanniao.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.minecraft.world.entity.player.Player;
import java.util.function.BiConsumer;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;

/**
 * Registers and routes every camera payload through one typed NeoForge channel.
 */
@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class NeoGuanNiaoNetwork {

    private NeoGuanNiaoNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("camera-1");
        registrar.playToServer(BeginPhotoUploadPacket.TYPE, BeginPhotoUploadPacket.STREAM_CODEC, adapt(BeginPhotoUploadPacket::handle));
        registrar.playToServer(PhotoUploadChunkPacket.TYPE, PhotoUploadChunkPacket.STREAM_CODEC, adapt(PhotoUploadChunkPacket::handle));
        registrar.playToServer(FinishPhotoUploadPacket.TYPE, FinishPhotoUploadPacket.STREAM_CODEC, adapt(FinishPhotoUploadPacket::handle));
        registrar.playToServer(PhotoRequestPacket.TYPE, PhotoRequestPacket.STREAM_CODEC, adapt(PhotoRequestPacket::handle));
        registrar.playToServer(SetCameraFilterPacket.TYPE, SetCameraFilterPacket.STREAM_CODEC, adapt(SetCameraFilterPacket::handle));
        registrar.playToServer(SetCameraSettingsPacket.TYPE, SetCameraSettingsPacket.STREAM_CODEC, adapt(SetCameraSettingsPacket::handle));
        registrar.playToClient(PhotoCaptureResultPacket.TYPE, PhotoCaptureResultPacket.STREAM_CODEC, adapt(PhotoCaptureResultPacket::handle));
        registrar.playToClient(PhotoDownloadStartPacket.TYPE, PhotoDownloadStartPacket.STREAM_CODEC, adapt(PhotoDownloadStartPacket::handle));
        registrar.playToClient(PhotoDownloadChunkPacket.TYPE, PhotoDownloadChunkPacket.STREAM_CODEC, adapt(PhotoDownloadChunkPacket::handle));
        registrar.playToServer(FeatherFanPiercePacket.TYPE, FeatherFanPiercePacket.STREAM_CODEC, adapt(FeatherFanPiercePacket::handle));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> adapt(BiConsumer<T, PayloadContext> handler) {
        return (payload, context) -> handler.accept(payload, new PayloadContext() {
            @Override
            public Player player() { return context.player(); }

            @Override
            public void enqueueWork(Runnable work) { context.enqueueWork(work); }
        });
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
