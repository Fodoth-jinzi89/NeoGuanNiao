package net.fodoth.skina.neoguanniao.mixin.client.gui;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gt$cameraHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CameraClientEvents.onRenderGuiOverlay()) {
            CameraClientEvents.onRenderGui(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
            ci.cancel();
        }
    }
}
