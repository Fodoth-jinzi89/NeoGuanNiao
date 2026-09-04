package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientCapture;
import net.fodoth.skina.neoguanniao.client.camera.CameraFilterPickerScreen;
import net.fodoth.skina.neoguanniao.client.camera.CameraOpticsShader;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilter;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilterCategory;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

public final class CameraPreviewPostEffect {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int THUMBNAIL_WIDTH = 160;
    private static final int THUMBNAIL_HEIGHT = 90;
    private static TextureTarget previewTarget;
    private static TextureTarget opticsTarget;
    private static PostChain chain;
    private static CameraFilter loadedFilter;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean preparedThisFrame;
    private static boolean cleanCapturePrepared;
    private static boolean opticsPassFailed;
    private static final Map<CameraFilter, ThumbnailSlot> THUMBNAILS;
    private static CameraFilterCategory thumbnailCategory;

    private CameraPreviewPostEffect() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void prepare(float partialTick) {
        if (!net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.ENABLE_PREVIEW_POST_EFFECT.get()) {
            preparedThisFrame = false;
            cleanCapturePrepared = false;
            return;
        }
        CameraFilterPickerScreen screen;
        CameraFilterPickerScreen picker;
        preparedThisFrame = false;
        cleanCapturePrepared = false;
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen2 = minecraft.screen;
        CameraFilterPickerScreen cameraFilterPickerScreen = picker = screen2 instanceof CameraFilterPickerScreen ? (screen = (CameraFilterPickerScreen)screen2) : null;
        if (picker == null) {
            CameraPreviewPostEffect.closeThumbnails();
        }
        boolean cleanCapture = CameraClientCapture.isCleanCapturePending();
        if (minecraft.level == null || !CameraClientCapture.isViewfinderOpen() && !cleanCapture) {
            return;
        }
        CameraState state = CameraClientCapture.renderState();
        CameraFilter filter = state.filter();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        int width = mainTarget.width;
        int height = mainTarget.height;
        if (width <= 0 || height <= 0) {
            return;
        }
        CameraPreviewPostEffect.ensureTarget(width, height);
        if (previewTarget == null || opticsTarget == null) {
            return;
        }
        try {
            boolean opticsRendered = false;
            if (!opticsPassFailed && net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.ENABLE_OPTICS_SHADER.get()) {
                try {
                    opticsRendered = CameraOpticsShader.process(mainTarget, (RenderTarget)opticsTarget, state);
                }
                catch (RuntimeException exception) {
                    opticsPassFailed = true;
                    LOGGER.error("Failed to process camera depth-of-field pass; using the unprocessed scene", (Throwable)exception);
                }
            }
            if (!opticsRendered) {
                CameraPreviewPostEffect.blitColor(mainTarget, (RenderTarget)opticsTarget);
            }
            if (cleanCapture) {
                cleanCapturePrepared = true;
                return;
            }
            CameraPreviewPostEffect.blitColor((RenderTarget)opticsTarget, (RenderTarget)previewTarget);
            preparedThisFrame = true;
            if (filter == CameraFilter.NONE || !net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.ENABLE_FILTER_PREVIEW.get()) {
                CameraPreviewPostEffect.closeChain();
            } else {
                if (loadedFilter != filter) {
                    CameraPreviewPostEffect.loadChain(filter);
                }
                if (chain != null) {
                    try {
                        chain.process(partialTick);
                    }
                    catch (RuntimeException exception) {
                        LOGGER.error("Failed to process camera preview filter {}", (Object)filter, (Object)exception);
                        CameraPreviewPostEffect.closeChain();
                        loadedFilter = filter;
                    }
                }
            }
            if (picker != null) {
                CameraPreviewPostEffect.prepareThumbnails(minecraft, (RenderTarget)opticsTarget, picker.previewCategory(), partialTick);
            }
        }
        finally {
            mainTarget.bindWrite(true);
        }
    }

    public static void drawFilteredLens(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (!preparedThisFrame || previewTarget == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        if (guiWidth <= 0 || guiHeight <= 0) {
            return;
        }
        float u0 = (float)left / (float)guiWidth;
        float u1 = (float)right / (float)guiWidth;
        float vTop = 1.0f - (float)top / (float)guiHeight;
        float vBottom = 1.0f - (float)bottom / (float)guiHeight;
        CameraPreviewPostEffect.drawPreviewTarget(graphics, (RenderTarget)previewTarget, left, top, right, bottom, u0, vBottom, u1, vTop);
    }

    public static void drawPreview(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (!preparedThisFrame || previewTarget == null || right <= left || bottom <= top) {
            return;
        }
        float sourceAspect = (float)CameraPreviewPostEffect.previewTarget.width / (float)Math.max(1, CameraPreviewPostEffect.previewTarget.height);
        float destinationAspect = (float)(right - left) / (float)Math.max(1, bottom - top);
        float u0 = 0.0f;
        float u1 = 1.0f;
        float vBottom = 0.0f;
        float vTop = 1.0f;
        if (sourceAspect > destinationAspect) {
            float crop;
            float visibleWidth = destinationAspect / sourceAspect;
            u0 = crop = (1.0f - visibleWidth) * 0.5f;
            u1 = 1.0f - crop;
        } else if (sourceAspect < destinationAspect) {
            float crop;
            float visibleHeight = sourceAspect / destinationAspect;
            vBottom = crop = (1.0f - visibleHeight) * 0.5f;
            vTop = 1.0f - crop;
        }
        CameraPreviewPostEffect.drawPreviewTarget(graphics, (RenderTarget)previewTarget, left, top, right, bottom, u0, vBottom, u1, vTop);
    }

    public static boolean drawFilterThumbnail(GuiGraphics graphics, CameraFilter filter, int left, int top, int right, int bottom) {
        ThumbnailSlot slot = THUMBNAILS.get((Object)filter);
        if (slot == null || !slot.ready || right <= left || bottom <= top) {
            return false;
        }
        CameraPreviewPostEffect.drawPreviewTarget(graphics, (RenderTarget)slot.target, left, top, right, bottom, 0.0f, 0.0f, 1.0f, 1.0f);
        return true;
    }

    private static void drawPreviewTarget(GuiGraphics graphics, RenderTarget target, int left, int top, int right, int bottom, float u0, float vBottom, float u1, float vTop) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture((int)0, (int)target.getColorTextureId());
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, (float)left, (float)bottom, 0.0f).setUv(u0, vBottom);
        builder.addVertex(matrix, (float)right, (float)bottom, 0.0f).setUv(u1, vBottom);
        builder.addVertex(matrix, (float)right, (float)top, 0.0f).setUv(u1, vTop);
        builder.addVertex(matrix, (float)left, (float)top, 0.0f).setUv(u0, vTop);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void ensureTarget(int width, int height) {
        if (previewTarget != null && opticsTarget != null && width == targetWidth && height == targetHeight) {
            return;
        }
        CameraPreviewPostEffect.closeChain();
        if (previewTarget != null) {
            previewTarget.destroyBuffers();
        }
        if (opticsTarget != null) {
            opticsTarget.destroyBuffers();
        }
        previewTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        previewTarget.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        opticsTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        opticsTarget.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        targetWidth = width;
        targetHeight = height;
    }

    private static void blitColor(RenderTarget source, RenderTarget destination) {
        RenderSystem.assertOnRenderThread();
        GL30.glBindFramebuffer((int)36008, (int)source.frameBufferId);
        GL30.glBindFramebuffer((int)36009, (int)destination.frameBufferId);
        GL30.glBlitFramebuffer((int)0, (int)0, (int)source.width, (int)source.height, (int)0, (int)0, (int)destination.width, (int)destination.height, (int)16384, (int)9728);
        GL30.glBindFramebuffer((int)36160, (int)destination.frameBufferId);
    }

    private static void blitCenterCropped(RenderTarget source, RenderTarget destination) {
        RenderSystem.assertOnRenderThread();
        float sourceAspect = (float)source.width / (float)Math.max(1, source.height);
        float destinationAspect = (float)destination.width / (float)Math.max(1, destination.height);
        int sourceLeft = 0;
        int sourceBottom = 0;
        int sourceRight = source.width;
        int sourceTop = source.height;
        if (sourceAspect > destinationAspect) {
            int croppedWidth = Math.max(1, Math.round((float)source.height * destinationAspect));
            sourceLeft = (source.width - croppedWidth) / 2;
            sourceRight = sourceLeft + croppedWidth;
        } else if (sourceAspect < destinationAspect) {
            int croppedHeight = Math.max(1, Math.round((float)source.width / destinationAspect));
            sourceBottom = (source.height - croppedHeight) / 2;
            sourceTop = sourceBottom + croppedHeight;
        }
        GL30.glBindFramebuffer((int)36008, (int)source.frameBufferId);
        GL30.glBindFramebuffer((int)36009, (int)destination.frameBufferId);
        GL30.glBlitFramebuffer((int)sourceLeft, (int)sourceBottom, (int)sourceRight, (int)sourceTop, (int)0, (int)0, (int)destination.width, (int)destination.height, (int)16384, (int)9729);
        GL30.glBindFramebuffer((int)36160, (int)destination.frameBufferId);
    }

    private static void prepareThumbnails(Minecraft minecraft, RenderTarget mainTarget, CameraFilterCategory category, float partialTick) {
        CameraPreviewPostEffect.ensureThumbnailCategory(minecraft, category);
        for (ThumbnailSlot slot : THUMBNAILS.values()) {
            slot.ready = false;
            if (slot.failed || slot.chain == null) continue;
            CameraPreviewPostEffect.blitCenterCropped(mainTarget, (RenderTarget)slot.target);
            try {
                slot.chain.process(partialTick);
                slot.ready = true;
            }
            catch (RuntimeException exception) {
                slot.failed = true;
                LOGGER.error("Failed to process camera filter thumbnail {}", (Object)slot.filter, (Object)exception);
                slot.closeChain();
            }
        }
    }

    private static void ensureThumbnailCategory(Minecraft minecraft, CameraFilterCategory category) {
        if (thumbnailCategory == category && THUMBNAILS.size() == CameraFilter.inCategory(category).size()) {
            return;
        }
        CameraPreviewPostEffect.closeThumbnails();
        thumbnailCategory = category;
        for (CameraFilter filter : CameraFilter.inCategory(category)) {
            TextureTarget target = new TextureTarget(160, 90, false, Minecraft.ON_OSX);
            target.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            ThumbnailSlot slot = new ThumbnailSlot(filter, target);
            THUMBNAILS.put(filter, slot);
            ResourceLocation postEffect = CameraPreviewPostEffect.postEffectFor(filter);
            try {
                slot.chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), (RenderTarget)target, postEffect);
                slot.chain.resize(160, 90);
            }
            catch (Exception exception) {
                slot.failed = true;
                LOGGER.error("Failed to load camera filter thumbnail effect {}", (Object)postEffect, (Object)exception);
                slot.closeChain();
            }
        }
    }

    private static void closeThumbnails() {
        for (ThumbnailSlot slot : THUMBNAILS.values()) {
            slot.close();
        }
        THUMBNAILS.clear();
        thumbnailCategory = null;
    }

    private static void loadChain(CameraFilter filter) {
        CameraPreviewPostEffect.closeChain();
        ResourceLocation postEffect = CameraPreviewPostEffect.postEffectFor(filter);
        if (postEffect == null || previewTarget == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        try {
            chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), (RenderTarget)previewTarget, postEffect);
            chain.resize(targetWidth, targetHeight);
            loadedFilter = filter;
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load camera preview post effect {}", (Object)postEffect, (Object)exception);
            CameraPreviewPostEffect.closeChain();
            loadedFilter = filter;
        }
    }

    private static ResourceLocation postEffectFor(CameraFilter filter) {
        if (filter == null || filter == CameraFilter.NONE) {
            return null;
        }
        String path = String.format(Locale.ROOT, "shaders/post/camera_filter_%02d.json", filter.id());
        return ResourceLocation.fromNamespaceAndPath("neoguanniao", path);
    }

    public static void close() {
        preparedThisFrame = false;
        cleanCapturePrepared = false;
        opticsPassFailed = false;
        CameraPreviewPostEffect.closeChain();
        CameraPreviewPostEffect.closeThumbnails();
        if (previewTarget != null) {
            previewTarget.destroyBuffers();
            previewTarget = null;
        }
        if (opticsTarget != null) {
            opticsTarget.destroyBuffers();
            opticsTarget = null;
        }
        targetWidth = -1;
        targetHeight = -1;
    }

    public static RenderTarget cleanCaptureTarget(RenderTarget fallback) {
        return cleanCapturePrepared && opticsTarget != null ? opticsTarget : fallback;
    }

    private static void closeChain() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        loadedFilter = CameraFilter.NONE;
    }

    static {
        loadedFilter = CameraFilter.NONE;
        targetWidth = -1;
        targetHeight = -1;
        THUMBNAILS = new EnumMap<CameraFilter, ThumbnailSlot>(CameraFilter.class);
    }

    private static final class ThumbnailSlot {
        private final CameraFilter filter;
        private final TextureTarget target;
        private PostChain chain;
        private boolean ready;
        private boolean failed;

        private ThumbnailSlot(CameraFilter filter, TextureTarget target) {
            this.filter = filter;
            this.target = target;
        }

        private void closeChain() {
            if (this.chain != null) {
                this.chain.close();
                this.chain = null;
            }
        }

        private void close() {
            this.closeChain();
            this.target.destroyBuffers();
        }
    }
}

