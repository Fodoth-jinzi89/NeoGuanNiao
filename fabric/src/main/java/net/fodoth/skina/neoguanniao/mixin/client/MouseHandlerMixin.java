package net.fodoth.skina.neoguanniao.mixin.client;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void gt$cameraButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow() && CameraClientEvents.onMouseButton(button, action)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void gt$cameraScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow() && CameraClientEvents.onMouseScroll(vertical)) {
            ci.cancel();
        }
    }
}
