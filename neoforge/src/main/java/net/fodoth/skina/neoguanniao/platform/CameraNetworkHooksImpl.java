package net.fodoth.skina.neoguanniao.platform;

import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoNetwork;
import net.fodoth.skina.neoguanniao.network.SetCameraSettingsPacket;
import net.minecraft.world.InteractionHand;

public final class CameraNetworkHooksImpl {
    private CameraNetworkHooksImpl() {}
    public static void sendSettings(InteractionHand hand, CameraState state) {
        NeoGuanNiaoNetwork.sendToServer(new SetCameraSettingsPacket(hand, state));
    }
}
