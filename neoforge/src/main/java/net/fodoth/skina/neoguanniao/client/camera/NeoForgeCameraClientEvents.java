package net.fodoth.skina.neoguanniao.client.camera;

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
public final class NeoForgeCameraClientEvents {
    private NeoForgeCameraClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        CameraClientEvents.onClientTick();
    }

    @SubscribeEvent
    public static void onRenderFrameStart(RenderFrameEvent.Pre event) {
        CameraClientEvents.onRenderFrameStart();
    }

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Post event) {
        CameraClientEvents.onRenderTick();
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(CameraClientEvents.onComputeFov(event.usedConfiguredFov(), event.getFOV()));
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (CameraClientEvents.shouldCancelRenderHand()) {
            event.setCanceled(true);
            CameraClientEvents.onRenderHand(event.getHand(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getSwingProgress(), event.getEquipProgress());
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Pre event) {
        if (CameraClientEvents.onRenderGuiOverlay()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        CameraClientEvents.onRenderGui(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            CameraClientEvents.onRenderLevelStage(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (CameraClientEvents.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (CameraClientEvents.onMouseButton(event.getButton(), event.getAction())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        CameraClientEvents.onKey(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CameraClientEvents.onClientLogout();
    }
}
