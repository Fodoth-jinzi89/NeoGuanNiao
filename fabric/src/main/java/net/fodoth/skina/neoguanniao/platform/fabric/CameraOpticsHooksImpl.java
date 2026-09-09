package net.fodoth.skina.neoguanniao.platform.fabric;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.fodoth.skina.neoguanniao.client.camera.CameraOpticsShader;
public final class CameraOpticsHooksImpl {
    private CameraOpticsHooksImpl() {}
    public static boolean process(RenderTarget source, RenderTarget destination, CameraState state) { return CameraOpticsShader.process(source, destination, state); }
}
