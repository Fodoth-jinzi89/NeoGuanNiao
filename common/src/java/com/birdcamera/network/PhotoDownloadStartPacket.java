package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PhotoDownloadStartPacket(String photoId, boolean found, int totalBytes, int width, int height, String contentHash)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PhotoDownloadStartPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("photo_download_start"));

    public static PhotoDownloadStartPacket missing(String photoId) {
        return new PhotoDownloadStartPacket(photoId, false, 0, 0, 0, "");
    }

    public static final StreamCodec<FriendlyByteBuf, PhotoDownloadStartPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
                buf.writeBoolean(packet.found);
                buf.writeVarInt(packet.totalBytes);
                buf.writeVarInt(packet.width);
                buf.writeVarInt(packet.height);
                buf.writeUtf(packet.contentHash, PhotoTransferLimits.SHA256_HEX_LENGTH);
            },
            buf -> new PhotoDownloadStartPacket(
                    buf.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(PhotoTransferLimits.SHA256_HEX_LENGTH)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}