package net.fodoth.skina.neoguanniao.mixin.client.renderer;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void gt$cameraFov(Camera camera, float partialTick, boolean useConfiguredFov, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(CameraClientEvents.onComputeFov(useConfiguredFov, cir.getReturnValue()));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void gt$frameStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        CameraClientEvents.onRenderFrameStart();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void gt$frameEnd(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        CameraClientEvents.onRenderTick();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void gt$worldRendered(DeltaTracker deltaTracker, CallbackInfo ci) {
        CameraClientEvents.onRenderLevelStage(deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
