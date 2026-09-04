package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

public final class CameraOpticsShader {
    private static final float NEAR_PLANE = 0.05f;
    private static ShaderInstance shader;

    private CameraOpticsShader() {
    }

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("neoguanniao", "camera_optics"), DefaultVertexFormat.POSITION_TEX), loaded -> {
            shader = loaded;
        });
    }

    public static boolean process(RenderTarget source, RenderTarget destination, CameraState state) {
        if (shader == null || source.getDepthTextureId() < 0 || source.width <= 0 || source.height <= 0) {
            return false;
        }
        RenderSystem.assertOnRenderThread();
        destination.bindWrite(true);
        shader.setSampler("DiffuseSampler", (Object)source.getColorTextureId());
        shader.setSampler("DepthSampler", (Object)source.getDepthTextureId());
        CameraOpticsShader.setUniform("OutSize", destination.width, destination.height);
        CameraOpticsShader.setUniform("NearPlane", 0.05f);
        float farPlane = Math.max(128.0f, (float)((Integer)Minecraft.getInstance().options.renderDistance().get()).intValue() * 16.0f);
        CameraOpticsShader.setUniform("FarPlane", farPlane);
        CameraOpticsShader.setUniform("FocusDistance", (float)state.focusDistance());
        CameraOpticsShader.setUniform("Aperture", state.aperture().fStop());
        CameraOpticsShader.setUniform("FocalLength", (float)state.focalLength());
        CameraOpticsShader.setUniform("DofMultiplier", state.lens().depthOfFieldMultiplier());
        CameraOpticsShader.setUniform("LensDistortion", state.lens().distortion());
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableBlend();
        RenderSystem.setShader(() -> shader);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0f, 0.0f);
        builder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0f, 0.0f);
        builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0f, 1.0f);
        builder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0f, 1.0f);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        return true;
    }

    private static void setUniform(String name, float value) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniform(String name, float first, float second) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(first, second);
        }
    }
}

