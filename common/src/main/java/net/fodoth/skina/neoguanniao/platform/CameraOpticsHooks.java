package net.fodoth.skina.neoguanniao.platform;
import dev.architectury.injectables.annotations.ExpectPlatform;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
public final class CameraOpticsHooks {
    private CameraOpticsHooks() {}
    @ExpectPlatform public static native boolean process(RenderTarget source, RenderTarget destination, CameraState state);
}
