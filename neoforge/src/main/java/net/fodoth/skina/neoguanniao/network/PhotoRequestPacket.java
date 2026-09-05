package net.fodoth.skina.neoguanniao.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PhotoRequestPacket(String photoId, String expectedHash) implements CustomPacketPayload {
    public static final Type<PhotoRequestPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoguanniao", "photo_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoRequestPacket> STREAM_CODEC = StreamCodec.of((b, p) -> {
        b.writeUtf(p.photoId, 80);
        b.writeUtf(p.expectedHash, 64);
    }, b -> new PhotoRequestPacket(b.readUtf(80), b.readUtf(64)));

    public static void handle(PhotoRequestPacket p, IPayloadContext c) {
        c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer s) PhotoUploadManager.requestDownload(s, p.photoId, p.expectedHash);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
