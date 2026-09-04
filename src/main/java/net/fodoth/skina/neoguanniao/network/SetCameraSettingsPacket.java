package net.fodoth.skina.neoguanniao.network;

import net.fodoth.skina.neoguanniao.content.camera.*;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SetCameraSettingsPacket(InteractionHand hand, CameraState state) implements CustomPacketPayload {
    public static final Type<SetCameraSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoguanniao", "set_camera_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCameraSettingsPacket> STREAM_CODEC = StreamCodec.of(SetCameraSettingsPacket::encode, SetCameraSettingsPacket::decode);
    private static void encode(RegistryFriendlyByteBuf buffer, SetCameraSettingsPacket packet) {
        CameraState state = packet.state;
        buffer.writeEnum(packet.hand); buffer.writeVarInt(state.filter().id()); buffer.writeVarInt(state.lens().id());
        buffer.writeVarInt(state.shootingMode().id()); buffer.writeDouble(state.focalLength()); buffer.writeVarInt(state.aperture().id());
        buffer.writeVarInt(state.focusMode().id()); buffer.writeDouble(state.focusDistance());
    }
    private static SetCameraSettingsPacket decode(RegistryFriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        return new SetCameraSettingsPacket(hand, new CameraState(CameraFilter.byId(buffer.readVarInt()), CameraLens.byId(buffer.readVarInt()), CameraShootingMode.byId(buffer.readVarInt()), buffer.readDouble(), CameraAperture.byId(buffer.readVarInt()), CameraFocusMode.byId(buffer.readVarInt()), buffer.readDouble()));
    }
    public static void handle(SetCameraSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> { if (context.player() instanceof ServerPlayer player) { ItemStack stack = player.getItemInHand(packet.hand); if (stack.is(NeoGuanNiaoItems.NIKON_D750.get())) CameraSettingsData.setState(stack, packet.state); } });
    }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
