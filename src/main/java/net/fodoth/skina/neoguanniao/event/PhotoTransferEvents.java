package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.content.camera.PhotoIndexSavedData;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIoService;
import net.fodoth.skina.neoguanniao.content.camera.PhotoMaintenance;
import net.fodoth.skina.neoguanniao.network.PhotoUploadManager;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="neoguanniao")
public final class PhotoTransferEvents {
    private static long nextMaintenanceTick;

    private PhotoTransferEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
            PhotoUploadManager.tick(event.getServer());
            long now = event.getServer().overworld().getGameTime();
            if (now >= nextMaintenanceTick && !PhotoMaintenance.isRunning()) {
                nextMaintenanceTick = now + 36000L;
                PhotoMaintenance.scheduleAutomatic(event.getServer(), result -> {});
            }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PhotoIndexSavedData.get(event.getServer());
        nextMaintenanceTick = event.getServer().overworld().getGameTime() + 200L;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PhotoUploadManager.disconnect(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PhotoUploadManager.clear();
        PhotoMaintenance.reset();
        PhotoIoService.shutdown();
        nextMaintenanceTick = 0L;
    }
}

