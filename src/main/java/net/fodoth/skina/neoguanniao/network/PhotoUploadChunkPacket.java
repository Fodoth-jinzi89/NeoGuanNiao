package net.fodoth.skina.neoguanniao.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record PhotoUploadChunkPacket(UUID uploadId, int chunkIndex, byte[] data) implements CustomPacketPayload {
    public static final Type<PhotoUploadChunkPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoguanniao", "photo_upload_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhotoUploadChunkPacket> STREAM_CODEC = StreamCodec.of((b, p) -> {
        b.writeUUID(p.uploadId);
        b.writeVarInt(p.chunkIndex);
        b.writeByteArray(p.data);
    }, b -> new PhotoUploadChunkPacket(b.readUUID(), b.readVarInt(), b.readByteArray(24576)));

    public static void handle(PhotoUploadChunkPacket p, IPayloadContext c) {
        c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer s)
                PhotoUploadManager.acceptChunk(s, p.uploadId, p.chunkIndex, p.data);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
