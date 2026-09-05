package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PhotoDownloadChunkPacket(String photoId, int chunkIndex, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PhotoDownloadChunkPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("photo_download_chunk"));

    public static final StreamCodec<FriendlyByteBuf, PhotoDownloadChunkPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                if (packet.data.length <= 0 || packet.data.length > PhotoTransferLimits.MAX_CHUNK_BYTES) {
                    throw new IllegalArgumentException("Invalid photograph chunk size");
                }
                buf.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
                buf.writeVarInt(packet.chunkIndex);
                buf.writeByteArray(packet.data);
            },
            buf -> new PhotoDownloadChunkPacket(
                    buf.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                    buf.readVarInt(),
                    buf.readByteArray(PhotoTransferLimits.MAX_CHUNK_BYTES)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}