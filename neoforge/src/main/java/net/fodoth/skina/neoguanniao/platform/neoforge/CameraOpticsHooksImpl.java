package net.fodoth.skina.neoguanniao.platform.neoforge;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.fodoth.skina.neoguanniao.client.camera.CameraOpticsShader;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
public final class CameraOpticsHooksImpl {
    private CameraOpticsHooksImpl() {}
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("neoguanniao", "camera_optics"), DefaultVertexFormat.POSITION_TEX), CameraOpticsShader::setShader);
    }
    public static boolean process(RenderTarget source, RenderTarget destination, CameraState state) { return CameraOpticsShader.process(source, destination, state); }
}
