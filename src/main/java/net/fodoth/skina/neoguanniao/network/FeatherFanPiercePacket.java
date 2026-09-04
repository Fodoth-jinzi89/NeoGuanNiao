package net.fodoth.skina.neoguanniao.network;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record FeatherFanPiercePacket() implements CustomPacketPayload {
    public static final Type<FeatherFanPiercePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "feather_fan_pierce"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.FriendlyByteBuf, FeatherFanPiercePacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.unit(new FeatherFanPiercePacket());

    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FeatherFanPiercePacket p, IPayloadContext c) {
        c.enqueueWork(() -> {
            if (c.player() instanceof net.minecraft.server.level.ServerPlayer sp && sp.getMainHandItem().getItem() instanceof FeatherFanItem fan)
                fan.tryLaunchPiercing(sp);
        });
    }
}
