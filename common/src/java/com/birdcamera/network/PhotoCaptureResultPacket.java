package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PhotoCaptureResultPacket(UUID uploadId, boolean success) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PhotoCaptureResultPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("photo_capture_result"));

    public static final StreamCodec<FriendlyByteBuf, PhotoCaptureResultPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.uploadId);
                buf.writeBoolean(packet.success);
            },
            buf -> new PhotoCaptureResultPacket(buf.readUUID(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}