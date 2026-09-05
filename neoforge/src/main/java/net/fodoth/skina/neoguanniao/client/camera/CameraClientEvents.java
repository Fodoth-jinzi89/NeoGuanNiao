package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="neoguanniao", value=Dist.CLIENT)
public final class CameraClientEvents {
    private CameraClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft;
            CameraClientCapture.tickViewfinder();
            while (CameraKeyMappings.OPEN_FILTER_LIBRARY.consumeClick()) {
                minecraft = Minecraft.getInstance();
                if (minecraft.screen != null || !CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) continue;
                CameraFilterPickerScreen.open();
            }
            while (CameraKeyMappings.OPEN_CREATIVE_CONTROLS.consumeClick()) {
                minecraft = Minecraft.getInstance();
                if (minecraft.screen != null || !CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) continue;
                CameraCreativeControlsScreen.open();
            }
            while (CameraKeyMappings.FOCUS.consumeClick()) {
                if (Minecraft.getInstance().screen != null) continue;
                CameraClientCapture.focusAtCrosshair();
            }
    }

    @SubscribeEvent
    public static void onRenderFrameStart(RenderFrameEvent.Pre event) {
        CameraClientCapture.onRenderTickStart();
    }

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Post event) {
        PhotographTextureCache.pumpUploads();
        CameraClientCapture.onRenderTickEnd();
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        double fov = CameraClientCapture.currentFovOverride(); if (event.usedConfiguredFov() && fov >= 0.0) event.setFOV(fov);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (CameraClientCapture.shouldHideHands()) {
            event.setCanceled(true);
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && CameraClientEvents.holdsCamera(player)) {
            event.setCanceled(true);
            if (CameraClientEvents.isCameraHand(player, event.getHand())) {
                CameraClientEvents.renderFirstPersonCamera(event, player, player.getItemInHand(event.getHand()));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Pre event) {
        if (CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        CameraClientCapture.renderViewfinder(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            CameraPreviewPostEffect.prepare(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return;
        }
        if (CameraClientCapture.handleMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return;
        }
        if (CameraClientCapture.handleMouseButton(event.getButton(), event.getAction())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != 1) {
            return;
        }
        if (CameraClientEvents.isCameraControlScreenOpen()) {
            return;
        }
        if (event.getKey() == 256 && CameraClientCapture.isViewfinderOpen()) {
            CameraClientCapture.closeViewfinder();
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CameraClientCapture.closeViewfinder();
        PhotographTextureCache.clear();
    }

    private static boolean holdsCamera(LocalPlayer player) {
        return player.getMainHandItem().is((Item)NeoGuanNiaoItems.NIKON_D750.get()) || player.getOffhandItem().is((Item)NeoGuanNiaoItems.NIKON_D750.get());
    }

    private static boolean isCameraControlScreenOpen() {
        return Minecraft.getInstance().screen instanceof CameraFilterPickerScreen || Minecraft.getInstance().screen instanceof CameraCreativeControlsScreen;
    }

    private static boolean isCameraHand(LocalPlayer player, InteractionHand hand) {
        return player.getItemInHand(hand).is((Item)NeoGuanNiaoItems.NIKON_D750.get());
    }

    private static void renderFirstPersonCamera(RenderHandEvent event, LocalPlayer player, ItemStack camera) {
        PoseStack poseStack = event.getPoseStack();
        Minecraft minecraft = Minecraft.getInstance();
        float equip = CameraClientEvents.equipAnimation(event);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.11f - equip * 0.86f, -0.88f + equip * 0.12f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-5.0f + equip * 22.0f));
        minecraft.getItemRenderer().renderStatic(camera, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, event.getPackedLight(), OverlayTexture.NO_OVERLAY, poseStack, event.getMultiBufferSource(), player.level(), player.getId());
        poseStack.popPose();
        EntityRenderer<? super LocalPlayer> renderer = minecraft.getEntityRenderDispatcher().getRenderer(player);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            CameraClientEvents.renderCameraArm(playerRenderer, player, poseStack, event, HumanoidArm.RIGHT);
            CameraClientEvents.renderCameraArm(playerRenderer, player, poseStack, event, HumanoidArm.LEFT);
        }
    }

    private static void renderCameraArm(PlayerRenderer renderer, LocalPlayer player, PoseStack poseStack, RenderHandEvent event, HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        float side = right ? 1.0f : -1.0f;
        float swing = Mth.clamp((float)event.getSwingProgress(), (float)0.0f, (float)0.25f);
        float rootSwing = Mth.sqrt((float)swing);
        float swingX = -0.18f * Mth.sin((float)(rootSwing * (float)Math.PI));
        float swingY = 0.14f * Mth.sin((float)(rootSwing * ((float)Math.PI * 2)));
        float swingZ = -0.18f * Mth.sin((float)(swing * (float)Math.PI));
        float equip = CameraClientEvents.equipAnimation(event);
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
            renderer.renderRightHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), (AbstractClientPlayer)player);
        } else {
            renderer.renderLeftHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), (AbstractClientPlayer)player);
        }
        poseStack.popPose();
    }

    private static float equipAnimation(RenderHandEvent event) {
        float equip = 1.0f - Mth.clamp((float)event.getEquipProgress(), (float)0.0f, (float)1.0f);
        return equip * equip * (3.0f - 2.0f * equip);
    }
}


