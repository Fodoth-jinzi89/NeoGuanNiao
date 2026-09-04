package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.CameraCreativeControlsScreen;
import net.fodoth.skina.neoguanniao.client.camera.CameraFilterPickerScreen;
import net.fodoth.skina.neoguanniao.client.camera.CameraImageFilters;
import net.fodoth.skina.neoguanniao.client.camera.CameraPreviewPostEffect;
import net.fodoth.skina.neoguanniao.client.camera.CameraViewfinderOverlay;
import net.fodoth.skina.neoguanniao.client.camera.PhotoClientRepository;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilter;
import net.fodoth.skina.neoguanniao.content.camera.CameraFocusMode;
import net.fodoth.skina.neoguanniao.content.camera.CameraSettingsData;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import net.fodoth.skina.neoguanniao.content.camera.PhotoImageCodec;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoNetwork;
import net.fodoth.skina.neoguanniao.network.SetCameraSettingsPacket;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class CameraClientCapture {
    private static final int CLEAN_CAPTURE_DELAY_FRAMES = 0;
    private static final double DEFAULT_FOCAL_LENGTH = 50.0;
    private static final double FOCAL_LENGTH_SCROLL_STEP = 4.0;
    private static final double FULL_FRAME_SENSOR_WIDTH = 36.0;
    private static final double MIN_VIEWFINDER_SENSITIVITY_SCALE = 0.28;
    private static final double MAX_AUTOFOCUS_DISTANCE = 128.0;
    private static boolean viewfinderOpen;
    private static InteractionHand viewfinderHand;
    private static CameraState currentState;
    private static int focusTargetId;
    private static int continuousFocusTicks;
    private static boolean cleanCapturePending;
    private static int cleanCaptureDelayFrames;
    private static InteractionHand pendingCaptureHand;
    private static double pendingCaptureFov;
    private static CameraState pendingCaptureState;
    private static float pendingCameraYRot;
    private static float pendingCameraXRot;
    private static boolean storedHideGui;
    private static CameraType storedCameraType;
    private static boolean sensitivityAdjusted;
    private static double storedSensitivity;
    private static boolean cleanFramePrepared;

    private CameraClientCapture() {
    }

    public static void openViewfinder(InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || cleanCapturePending) {
            return;
        }
        if (viewfinderOpen) {
            CameraClientCapture.closeViewfinder();
            return;
        }
        viewfinderHand = hand;
        currentState = CameraSettingsData.state(minecraft.player.getItemInHand(hand));
        focusTargetId = -1;
        continuousFocusTicks = 0;
        CameraClientCapture.beginSensitivityAdjustment(minecraft);
        CameraClientCapture.applyFocalSensitivity(minecraft);
        viewfinderOpen = true;
        minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.35f, 1.5f);
    }

    public static boolean isViewfinderOpen() {
        return viewfinderOpen;
    }

    public static CameraFilter currentFilter() {
        return currentState.filter();
    }

    public static CameraState currentState() {
        return currentState;
    }

    static CameraState renderState() {
        return cleanCapturePending ? pendingCaptureState : currentState;
    }

    public static boolean shouldHideHands() {
        return (viewfinderOpen || cleanCapturePending) && net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.HIDE_HAND.get();
    }

    public static boolean isCleanCapturePending() {
        return cleanCapturePending;
    }

    public static void closeViewfinder() {
        CameraClientCapture.persistCurrentState();
        viewfinderOpen = false;
        focusTargetId = -1;
        CameraPreviewPostEffect.close();
        CameraClientCapture.restoreSensitivity(Minecraft.getInstance());
    }

    public static void tickViewfinder() {
        if (!viewfinderOpen) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CameraClientCapture.isCameraStillHeld(minecraft, viewfinderHand)) {
            CameraClientCapture.closeViewfinder();
            return;
        }
        if (currentState.focusMode() == CameraFocusMode.AF_C) {
            CameraClientCapture.tickContinuousFocus(minecraft);
        } else {
            focusTargetId = -1;
        }
    }

    public static boolean handleMouseScroll(double delta) {
        if (!viewfinderOpen || cleanCapturePending) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        double scrollAmount = Math.max(1.0, Math.abs(delta));
        if (currentState.focusMode() == CameraFocusMode.MANUAL && minecraft.options.keyShift.isDown()) {
            double factor = Math.pow(1.16, scrollAmount * (delta > 0.0 ? 1.0 : -1.0));
            currentState = currentState.withFocusDistance(currentState.focusDistance() * factor);
        } else {
            double next = currentState.focalLength() + (delta > 0.0 ? 4.0 : -4.0) * scrollAmount;
            currentState = currentState.withFocalLength(next);
            CameraClientCapture.applyFocalSensitivity(minecraft);
        }
        if (minecraft.player != null) {
            minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.18f, delta > 0.0 ? 1.65f : 1.1f);
        }
        return true;
    }

    public static boolean handleMouseButton(int button, int action) {
        if (!viewfinderOpen || cleanCapturePending || action != 1) {
            return false;
        }
        if (button == 0) {
            CameraClientCapture.beginCleanCapture(viewfinderHand, CameraClientCapture.currentFov());
            return true;
        }
        if (button == 1) {
            CameraClientCapture.closeViewfinder();
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.25f, 0.85f);
            }
            return true;
        }
        return false;
    }

    public static boolean previewFilter(CameraFilter filter) {
        if (!viewfinderOpen || cleanCapturePending || filter == null) {
            return false;
        }
        currentState = currentState.withFilter(filter);
        return true;
    }

    public static boolean commitFilter(CameraFilter filter) {
        if (!viewfinderOpen || cleanCapturePending || filter == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CameraClientCapture.isCameraStillHeld(minecraft, viewfinderHand)) {
            CameraClientCapture.closeViewfinder();
            return false;
        }
        currentState = currentState.withFilter(filter);
        CameraClientCapture.persistCurrentState();
        minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.28f, 1.0f + (float)(currentState.filter().id() % 8) * 0.035f);
        return true;
    }

    public static boolean previewState(CameraState state) {
        if (!viewfinderOpen || cleanCapturePending || state == null) {
            return false;
        }
        currentState = state;
        CameraClientCapture.applyFocalSensitivity(Minecraft.getInstance());
        return true;
    }

    public static boolean commitState(CameraState state) {
        if (!CameraClientCapture.previewState(state)) {
            return false;
        }
        CameraClientCapture.persistCurrentState();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 1.25f);
        }
        return true;
    }

    public static void restorePreviewState(CameraState state) {
        if (state != null && viewfinderOpen && !cleanCapturePending) {
            currentState = state;
            focusTargetId = -1;
            CameraClientCapture.applyFocalSensitivity(Minecraft.getInstance());
        }
    }

    public static void restorePreviewFilter(CameraFilter original) {
        if (original != null && viewfinderOpen && !cleanCapturePending) {
            currentState = currentState.withFilter(original);
        }
    }

    public static boolean focusAtCrosshair() {
        if (!viewfinderOpen || cleanCapturePending) {
            return false;
        }
        return CameraClientCapture.updateFocusFromCrosshair(Minecraft.getInstance(), true);
    }

    public static void renderViewfinder(GuiGraphics graphics, float partialTick) {
        if (!viewfinderOpen || cleanCapturePending || Minecraft.getInstance().screen instanceof CameraFilterPickerScreen || Minecraft.getInstance().screen instanceof CameraCreativeControlsScreen) {
            return;
        }
        CameraViewfinderOverlay.render(graphics, currentState, CameraClientCapture.currentFov());
    }

    public static void modifyFov(ViewportEvent.ComputeFov event) {
        if (!event.usedConfiguredFov()) {
            return;
        }
        if (viewfinderOpen) {
            event.setFOV(CameraClientCapture.currentFov());
        } else if (cleanCapturePending) {
            event.setFOV(pendingCaptureFov);
        }
    }

    public static void onRenderTickStart() {
        if (!cleanCapturePending) {
            return;
        }
        if (cleanCaptureDelayFrames-- > 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            CameraClientCapture.restoreAfterCleanCapture();
            return;
        }
        minecraft.player.setYRot(pendingCameraYRot);
        minecraft.player.yRotO = pendingCameraYRot;
        minecraft.player.setXRot(pendingCameraXRot);
        minecraft.player.xRotO = pendingCameraXRot;
        minecraft.gameRenderer.setRenderBlockOutline(false);
        cleanFramePrepared = true;
    }

    public static void onRenderTickEnd() {
        if (!cleanCapturePending || !cleanFramePrepared) {
            return;
        }
        try {
            CameraClientCapture.captureAndSend(pendingCaptureHand);
        }
        finally {
            CameraClientCapture.restoreAfterCleanCapture();
        }
    }

    public static void captureAndSend(InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        RenderTarget captureTarget = CameraPreviewPostEffect.cleanCaptureTarget(minecraft.getMainRenderTarget());
        try (NativeImage image = Screenshot.takeScreenshot((RenderTarget)captureTarget);){
            int[] pixels = CameraClientCapture.cropSquare(image, 1024);
            CameraImageFilters.apply(pixels, 1024, 1024, pendingCaptureState.filter(), System.nanoTime());
            byte[] jpeg = PhotoImageCodec.encodeJpeg(pixels, 1024, 1024);
            PhotoClientRepository.upload(hand, jpeg);
        }
        catch (Exception exception) {
            minecraft.player.displayClientMessage((Component)Component.translatable((String)"item.neoguanniao.nikon_d750.capture_failed"), true);
        }
    }

    private static int[] cropSquare(NativeImage image, int size) {
        int sourceWidth = image.getWidth();
        int sourceHeight = image.getHeight();
        int sourceSize = CameraViewfinderOverlay.apertureSize(sourceWidth, sourceHeight);
        int offsetX = (sourceWidth - sourceSize) / 2;
        int offsetY = (sourceHeight - sourceSize) / 2;
        int[] pixels = new int[size * size];
        for (int y = 0; y < size; ++y) {
            int sampleY = offsetY + Math.min(sourceSize - 1, (int)(((double)y + 0.5) * (double)sourceSize / (double)size));
            for (int x = 0; x < size; ++x) {
                int sampleX = offsetX + Math.min(sourceSize - 1, (int)(((double)x + 0.5) * (double)sourceSize / (double)size));
                pixels[y * size + x] = image.getPixelRGBA(sampleX, sampleY);
            }
        }
        return pixels;
    }

    private static void beginCleanCapture(InteractionHand hand, double fov) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CameraClientCapture.isCameraStillHeld(minecraft, hand)) {
            CameraClientCapture.closeViewfinder();
            return;
        }
        viewfinderOpen = false;
        CameraPreviewPostEffect.close();
        cleanCapturePending = true;
        cleanFramePrepared = false;
        cleanCaptureDelayFrames = 0;
        pendingCaptureHand = hand;
        pendingCaptureFov = fov;
        pendingCaptureState = currentState;
        CameraClientCapture.persistCurrentState();
        pendingCameraYRot = minecraft.player.getViewYRot(1.0f);
        pendingCameraXRot = minecraft.player.getViewXRot(1.0f);
        storedHideGui = minecraft.options.hideGui;
        storedCameraType = minecraft.options.getCameraType();
        minecraft.options.hideGui = net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.HIDE_GUI.get();
        if (storedCameraType != CameraType.THIRD_PERSON_FRONT) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
        minecraft.player.playSound(SoundEvents.SPYGLASS_USE, 0.45f, 1.4f);
    }

    private static void restoreAfterCleanCapture() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.hideGui = storedHideGui;
        minecraft.options.setCameraType(storedCameraType);
        minecraft.gameRenderer.setRenderBlockOutline(true);
        CameraClientCapture.restoreSensitivity(minecraft);
        cleanFramePrepared = false;
        cleanCapturePending = false;
    }

    private static boolean isCameraStillHeld(Minecraft minecraft, InteractionHand hand) {
        if (minecraft.player == null) {
            return false;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.is((Item)NeoGuanNiaoItems.NIKON_D750.get());
    }

    private static double currentFov() {
        return CameraClientCapture.focalLengthToFov(currentState.focalLength());
    }

    private static double focalLengthToFov(double focalLength) {
        double fov = Math.toDegrees(2.0 * Math.atan(36.0 / (2.0 * focalLength)));
        return Mth.clamp((double)fov, (double)14.0, (double)110.0);
    }

    private static void beginSensitivityAdjustment(Minecraft minecraft) {
        if (!sensitivityAdjusted) {
            storedSensitivity = (Double)minecraft.options.sensitivity().get();
            sensitivityAdjusted = true;
        }
    }

    private static void applyFocalSensitivity(Minecraft minecraft) {
        if (!sensitivityAdjusted) {
            return;
        }
        minecraft.options.sensitivity().set(storedSensitivity * CameraClientCapture.focalSensitivityScale());
    }

    private static void restoreSensitivity(Minecraft minecraft) {
        if (!sensitivityAdjusted) {
            return;
        }
        minecraft.options.sensitivity().set(storedSensitivity);
        sensitivityAdjusted = false;
    }

    private static double focalSensitivityScale() {
        double normalized = Mth.clamp((double)((currentState.focalLength() - 8.0) / 192.0), (double)0.0, (double)1.0);
        double smooth = normalized * normalized * (3.0 - 2.0 * normalized);
        return Mth.lerp((double)smooth, (double)1.0, (double)0.28);
    }

    private static void tickContinuousFocus(Minecraft minecraft) {
        double distance;
        Entity target;
        Entity entity = target = minecraft.level == null || focusTargetId < 0 ? null : minecraft.level.getEntity(focusTargetId);
        if (target != null && target.isAlive() && minecraft.player != null && (distance = minecraft.player.getEyePosition(1.0f).distanceTo(target.getBoundingBox().getCenter())) <= 128.0) {
            currentState = currentState.withFocusDistance(distance);
            return;
        }
        focusTargetId = -1;
        if (++continuousFocusTicks >= 5) {
            continuousFocusTicks = 0;
            CameraClientCapture.updateFocusFromCrosshair(minecraft, false);
        }
    }

    private static boolean updateFocusFromCrosshair(Minecraft minecraft, boolean feedback) {
        HitResult hit;
        Vec3 look;
        Vec3 end;
        if (minecraft.player == null || minecraft.level == null) {
            return false;
        }
        Vec3 eye = minecraft.player.getEyePosition(1.0f);
        BlockHitResult blockHit = minecraft.level.clip(new ClipContext(eye, end = eye.add((look = minecraft.player.getViewVector(1.0f)).scale(128.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)minecraft.player));
        double blockDistanceSquared = blockHit.getType() == HitResult.Type.MISS ? 16384.0 : eye.distanceToSqr(blockHit.getLocation());
        Vec3 entityRayEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        AABB searchArea = minecraft.player.getBoundingBox().expandTowards(entityRayEnd.subtract(eye)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult((Level)minecraft.level, (Entity)minecraft.player, (Vec3)eye, (Vec3)entityRayEnd, (AABB)searchArea, entity -> !entity.isSpectator() && entity.isPickable());
        hit = entityHit != null && eye.distanceToSqr(entityHit.getLocation()) <= blockDistanceSquared ? entityHit : blockHit;
        if (hit.getType() == HitResult.Type.MISS) {
            currentState = currentState.withFocusDistance(128.0);
            focusTargetId = -1;
            if (feedback) {
                minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.22f, 1.45f);
            }
            return true;
        }
        double distance = eye.distanceTo(hit.getLocation());
        if ((currentState = currentState.withFocusDistance(distance)).focusMode() == CameraFocusMode.AF_C && hit instanceof EntityHitResult) {
            EntityHitResult focusedEntityHit = (EntityHitResult)hit;
            focusTargetId = focusedEntityHit.getEntity().getId();
        } else {
            focusTargetId = -1;
        }
        if (feedback) {
            minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.22f, 1.8f);
        }
        return true;
    }

    private static void persistCurrentState() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CameraClientCapture.isCameraStillHeld(minecraft, viewfinderHand)) {
            return;
        }
        ItemStack camera = minecraft.player.getItemInHand(viewfinderHand);
        CameraSettingsData.setState(camera, currentState);
        NeoGuanNiaoNetwork.sendToServer(new SetCameraSettingsPacket(viewfinderHand, currentState));
    }

    static {
        viewfinderHand = InteractionHand.MAIN_HAND;
        currentState = CameraState.defaults();
        focusTargetId = -1;
        pendingCaptureHand = InteractionHand.MAIN_HAND;
        pendingCaptureFov = CameraClientCapture.focalLengthToFov(50.0);
        pendingCaptureState = CameraState.defaults();
        storedCameraType = CameraType.FIRST_PERSON;
    }
}

