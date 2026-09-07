package net.fodoth.skina.neoguanniao.platform.fabric;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class NetworkHooksImpl {
    private NetworkHooksImpl() {}

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        throw new UnsupportedOperationException("Photo payload registration is not implemented on Fabric yet");
    }
}
