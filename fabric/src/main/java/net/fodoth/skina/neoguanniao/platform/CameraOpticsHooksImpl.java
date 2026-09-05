package net.fodoth.skina.neoguanniao.platform;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
public final class CameraOpticsHooksImpl {
    private CameraOpticsHooksImpl() {}
    public static boolean process(RenderTarget source, RenderTarget destination, CameraState state) { return false; }
}
