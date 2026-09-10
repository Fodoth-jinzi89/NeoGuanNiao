package net.fodoth.skina.neoguanniao.platform.fabric;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetworkHooksImpl {
    private NetworkHooksImpl() {}

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
