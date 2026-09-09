package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class CameraClientEvents {
    private static boolean attackWasDown;
    private static int debugTick;

    private CameraClientEvents() {
    }

    public static void onClientTick() {
        onClientTick(true);
    }

    public static void onClientTick(boolean pollCapture) {
        Minecraft minecraft;
        CameraClientCapture.tickViewfinder();
        debugTick++;
        minecraft = Minecraft.getInstance();
        if (CameraClientCapture.isViewfinderOpen()) {
            ++debugTick;
        }
        boolean attackDown = minecraft.options.keyAttack.isDown()
                || GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (pollCapture && CameraClientCapture.isViewfinderOpen() && minecraft.screen == null && attackDown && !attackWasDown) {
            CameraClientCapture.handleMouseButton(0, 1);
        }
        attackWasDown = attackDown;
        if (!CameraClientCapture.isViewfinderOpen()) {
            attackWasDown = false;
        }
        if (pollCapture && CameraClientCapture.isCleanCapturePending()) {
            CameraClientCapture.captureImmediately();
        }
        while (CameraKeyMappings.OPEN_FILTER_LIBRARY.consumeClick()) {
            minecraft = Minecraft.getInstance();
            if (minecraft.screen != null || !CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending())
                continue;
            CameraFilterPickerScreen.open();
        }
        while (CameraKeyMappings.OPEN_CREATIVE_CONTROLS.consumeClick()) {
            minecraft = Minecraft.getInstance();
            if (minecraft.screen != null || !CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending())
                continue;
            CameraCreativeControlsScreen.open();
        }
        while (CameraKeyMappings.FOCUS.consumeClick()) {
            if (Minecraft.getInstance().screen != null) continue;
            CameraClientCapture.focusAtCrosshair();
        }
    }

    public static void onRenderFrameStart() {
        CameraClientCapture.onRenderTickStart();
    }

    public static void onRenderTick() {
        PhotographTextureCache.pumpUploads();
        CameraClientCapture.onRenderTickEnd();
    }

    public static double onComputeFov(boolean usedConfiguredFov, double originalFov) {
        double fov = CameraClientCapture.currentFovOverride();
        return usedConfiguredFov && fov >= 0.0 ? fov : originalFov;
    }

    public static boolean shouldCancelRenderHand() {
        LocalPlayer player = Minecraft.getInstance().player;
        return CameraClientCapture.shouldHideHands() || player != null && holdsCamera(player);
    }

    public static void onRenderHand(InteractionHand hand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float swingProgress, float equipProgress) {
        if (CameraClientCapture.shouldHideHands()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && CameraClientEvents.holdsCamera(player) && CameraClientEvents.isCameraHand(player, hand)) {
            CameraClientEvents.renderFirstPersonCamera(poseStack, bufferSource, packedLight, swingProgress, equipProgress, player, player.getItemInHand(hand));
        }
    }

    public static boolean onRenderGuiOverlay() {
        return CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending();
    }

    public static void onRenderGui(GuiGraphics graphics, float partialTick) {
        CameraClientCapture.renderViewfinder(graphics, partialTick);
    }

    public static void onRenderLevelStage(float partialTick) {
        CameraPreviewPostEffect.prepare(partialTick);
    }

    public static boolean onMouseScroll(double scrollDeltaY) {
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return false;
        }
        return CameraClientCapture.handleMouseScroll(scrollDeltaY);
    }

    public static boolean onMouseButton(int button, int action) {
        if (CameraClientCapture.isViewfinderOpen()) {
            if (button == 0 && action == 1 && Minecraft.getInstance().screen == null) {
                CameraClientCapture.handleMouseButton(0, 1);
                return true;
            }
        }
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return false;
        }
        return CameraClientCapture.handleMouseButton(button, action);
    }

    public static void onKey(int key, int action) {
        if (action != 1) {
            return;
        }
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return;
        }
        if (key == 256 && CameraClientCapture.isViewfinderOpen()) {
            CameraClientCapture.closeViewfinder();
        }
    }

    public static void onClientLogout() {
        CameraClientCapture.closeViewfinder();
        PhotographTextureCache.clear();
    }

    private static boolean holdsCamera(LocalPlayer player) {
        return player.getMainHandItem().is(NeoGuanNiaoItems.NIKON_D750.get()) || player.getOffhandItem().is(NeoGuanNiaoItems.NIKON_D750.get());
    }

    private static boolean isCameraControlScreenOpen() {
        return Minecraft.getInstance().screen instanceof CameraFilterPickerScreen || Minecraft.getInstance().screen instanceof CameraCreativeControlsScreen;
    }

    private static boolean isCameraHand(LocalPlayer player, InteractionHand hand) {
        return player.getItemInHand(hand).is(NeoGuanNiaoItems.NIKON_D750.get());
    }

    private static void renderFirstPersonCamera(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float swingProgress, float equipProgress, LocalPlayer player, ItemStack camera) {
        Minecraft minecraft = Minecraft.getInstance();
        float equip = CameraClientEvents.equipAnimation(equipProgress);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.11f - equip * 0.86f, -0.88f + equip * 0.12f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-5.0f + equip * 22.0f));
        minecraft.getItemRenderer().renderStatic(camera, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, player.level(), player.getId());
        poseStack.popPose();
        EntityRenderer<? super LocalPlayer> renderer = minecraft.getEntityRenderDispatcher().getRenderer(player);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            CameraClientEvents.renderCameraArm(playerRenderer, player, poseStack, bufferSource, packedLight, swingProgress, equipProgress, HumanoidArm.RIGHT);
            CameraClientEvents.renderCameraArm(playerRenderer, player, poseStack, bufferSource, packedLight, swingProgress, equipProgress, HumanoidArm.LEFT);
        }
    }

    private static void renderCameraArm(PlayerRenderer renderer, LocalPlayer player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float swingProgress, float equipProgress, HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        float side = right ? 1.0f : -1.0f;
        float swing = Mth.clamp(swingProgress, 0.0f, 0.25f);
        float rootSwing = Mth.sqrt(swing);
        float swingX = -0.18f * Mth.sin(rootSwing * (float) Math.PI);
        float swingY = 0.14f * Mth.sin(rootSwing * ((float) Math.PI * 2));
        float swingZ = -0.18f * Mth.sin(swing * (float) Math.PI);
        float equip = CameraClientEvents.equipAnimation(equipProgress);
        poseStack.pushPose();
        poseStack.translate(side * (0.84f + swingX + equip * 0.14f), -0.44f + swingY - equip * 0.82f, -1.0f + swingZ + equip * 0.08f);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * (32.0f + equip * 14.0f)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * (-4.0f - equip * 12.0f)));
        poseStack.translate(side * -1.0f, 3.45f, 3.35f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 112.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(205.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -124.0f));
        poseStack.translate(side * 5.45f, -0.2f, 0.0f);
        if (right) {
            renderer.renderRightHand(poseStack, bufferSource, packedLight, player);
        } else {
            renderer.renderLeftHand(poseStack, bufferSource, packedLight, player);
        }
        poseStack.popPose();
    }

    private static float equipAnimation(float equipProgress) {
        float equip = 1.0f - Mth.clamp(equipProgress, 0.0f, 1.0f);
        return equip * equip * (3.0f - 2.0f * equip);
    }
}


