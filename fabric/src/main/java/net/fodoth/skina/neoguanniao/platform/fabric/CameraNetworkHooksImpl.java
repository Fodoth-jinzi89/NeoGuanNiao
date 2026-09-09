package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.minecraft.world.InteractionHand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fodoth.skina.neoguanniao.network.SetCameraSettingsPacket;

public final class CameraNetworkHooksImpl {
    private CameraNetworkHooksImpl() {
    }

    public static void sendSettings(InteractionHand hand, CameraState state) {
        if (ClientPlayNetworking.canSend(SetCameraSettingsPacket.TYPE)) {
            ClientPlayNetworking.send(new SetCameraSettingsPacket(hand, state));
        }
    }
}
