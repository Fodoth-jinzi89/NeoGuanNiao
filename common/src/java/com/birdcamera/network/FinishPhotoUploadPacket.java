package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FinishPhotoUploadPacket(UUID uploadId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FinishPhotoUploadPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("finish_photo_upload"));

    public static final StreamCodec<FriendlyByteBuf, FinishPhotoUploadPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUUID(packet.uploadId),
            buf -> new FinishPhotoUploadPacket(buf.readUUID()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}