package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class NetworkHooks {
    private NetworkHooks() {}

    @ExpectPlatform
    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) { throw new AssertionError(); }
}
