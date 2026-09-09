package net.fodoth.skina.neoguanniao.mixin.client;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientCapture;
import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void gt$cameraKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (window != Minecraft.getInstance().getWindow().getWindow()) return;
        boolean wasOpen = CameraClientCapture.isViewfinderOpen();
        CameraClientEvents.onKey(key, action);
        if (wasOpen && !CameraClientCapture.isViewfinderOpen()) ci.cancel();
    }
}
