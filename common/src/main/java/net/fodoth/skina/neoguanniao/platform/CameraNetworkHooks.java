package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.minecraft.world.InteractionHand;

public final class CameraNetworkHooks {
    private CameraNetworkHooks() {}
    @ExpectPlatform public static native void sendSettings(InteractionHand hand, CameraState state);
}
