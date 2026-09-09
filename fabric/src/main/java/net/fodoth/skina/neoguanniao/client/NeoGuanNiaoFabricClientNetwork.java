package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fodoth.skina.neoguanniao.network.PayloadContext;
import net.fodoth.skina.neoguanniao.network.PhotoCaptureResultPacket;
import net.fodoth.skina.neoguanniao.network.PhotoDownloadChunkPacket;
import net.fodoth.skina.neoguanniao.network.PhotoDownloadStartPacket;
import net.minecraft.client.Minecraft;

public final class NeoGuanNiaoFabricClientNetwork {
    private NeoGuanNiaoFabricClientNetwork() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PhotoCaptureResultPacket.TYPE,
                (packet, context) -> PhotoCaptureResultPacket.handle(packet, payloadContext()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadStartPacket.TYPE,
                (packet, context) -> PhotoDownloadStartPacket.handle(packet, payloadContext()));
        ClientPlayNetworking.registerGlobalReceiver(PhotoDownloadChunkPacket.TYPE,
                (packet, context) -> PhotoDownloadChunkPacket.handle(packet, payloadContext()));
    }

    private static PayloadContext payloadContext() {
        var minecraft = Minecraft.getInstance();
        return new PayloadContext() {
            public net.minecraft.world.entity.player.Player player() {
                return minecraft.player;
            }

            public void enqueueWork(Runnable work) {
                minecraft.execute(work);
            }
        };
    }
}
