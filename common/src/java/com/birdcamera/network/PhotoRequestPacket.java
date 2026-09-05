package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.camera.PhotoTransferLimits;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PhotoRequestPacket(String photoId, String expectedHash) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PhotoRequestPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("photo_request"));

    public static final StreamCodec<FriendlyByteBuf, PhotoRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.photoId, PhotoTransferLimits.MAX_PHOTO_ID_LENGTH);
                buf.writeUtf(packet.expectedHash, PhotoTransferLimits.SHA256_HEX_LENGTH);
            },
            buf -> new PhotoRequestPacket(
                    buf.readUtf(PhotoTransferLimits.MAX_PHOTO_ID_LENGTH),
                    buf.readUtf(PhotoTransferLimits.SHA256_HEX_LENGTH)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}