package com.birdcamera.network;

import com.birdcamera.BirdCameraMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record BeginPhotoUploadPacket(UUID uploadId, InteractionHand hand, int totalBytes, int width, int height, String contentHash)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BeginPhotoUploadPacket> TYPE =
            new CustomPacketPayload.Type<>(BirdCameraMod.id("begin_photo_upload"));

    public static final StreamCodec<FriendlyByteBuf, BeginPhotoUploadPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUUID(packet.uploadId);
                buf.writeEnum(packet.hand);
                buf.writeVarInt(packet.totalBytes);
                buf.writeVarInt(packet.width);
                buf.writeVarInt(packet.height);
                buf.writeUtf(packet.contentHash, 64);
            },
            buf -> new BeginPhotoUploadPacket(
                    buf.readUUID(),
                    buf.readEnum(InteractionHand.class),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}