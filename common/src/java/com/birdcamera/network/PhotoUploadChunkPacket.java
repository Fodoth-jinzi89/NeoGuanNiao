package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.camera.PhotoTransferLimits;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PhotoUploadChunkPacket(UUID uploadId, int chunkIndex, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PhotoUploadChunkPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("photo_upload_chunk"));

    public static final StreamCodec<FriendlyByteBuf, PhotoUploadChunkPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                if (packet.data.length <= 0 || packet.data.length > PhotoTransferLimits.MAX_CHUNK_BYTES) {
                    throw new IllegalArgumentException("Invalid photograph chunk size");
                }
                buf.writeUUID(packet.uploadId);
                buf.writeVarInt(packet.chunkIndex);
                buf.writeByteArray(packet.data);
            },
            buf -> new PhotoUploadChunkPacket(
                    buf.readUUID(),
                    buf.readVarInt(),
                    buf.readByteArray(PhotoTransferLimits.MAX_CHUNK_BYTES)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}