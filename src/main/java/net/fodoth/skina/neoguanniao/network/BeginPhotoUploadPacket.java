package net.fodoth.skina.neoguanniao.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BeginPhotoUploadPacket(UUID uploadId, InteractionHand hand, int totalBytes, int width, int height,
                                     String contentHash) implements CustomPacketPayload {
    public static final Type<BeginPhotoUploadPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoguanniao", "begin_photo_upload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BeginPhotoUploadPacket> STREAM_CODEC = StreamCodec.of(BeginPhotoUploadPacket::encode, BeginPhotoUploadPacket::decode);
    private static void encode(RegistryFriendlyByteBuf buffer, BeginPhotoUploadPacket packet) { buffer.writeUUID(packet.uploadId); buffer.writeEnum(packet.hand); buffer.writeVarInt(packet.totalBytes); buffer.writeVarInt(packet.width); buffer.writeVarInt(packet.height); buffer.writeUtf(packet.contentHash, 64); }
    private static BeginPhotoUploadPacket decode(RegistryFriendlyByteBuf buffer) { return new BeginPhotoUploadPacket(buffer.readUUID(), buffer.readEnum(InteractionHand.class), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(64)); }
    public static void handle(BeginPhotoUploadPacket packet, IPayloadContext context) { context.enqueueWork(() -> { if (context.player() instanceof ServerPlayer player) PhotoUploadManager.begin(player, packet.uploadId, packet.hand, packet.totalBytes, packet.width, packet.height, packet.contentHash); }); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
