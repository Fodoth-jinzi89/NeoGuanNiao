package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.network.PhotoUploadManager;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="neoguanniao")
public final class NeoForgePhotoTransferEvents {
    private NeoForgePhotoTransferEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PhotoUploadManager.tick(event.getServer());
        PhotoTransferEvents.onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PhotoTransferEvents.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PhotoUploadManager.disconnect(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PhotoUploadManager.clear();
        PhotoTransferEvents.onServerStopping();
    }
}
