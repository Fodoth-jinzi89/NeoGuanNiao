package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.content.camera.PhotoIndexSavedData;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIoService;
import net.fodoth.skina.neoguanniao.content.camera.PhotoMaintenance;
import net.minecraft.server.MinecraftServer;

public final class PhotoTransferEvents {
    private static long nextMaintenanceTick;

    private PhotoTransferEvents() {
    }

    public static void onServerTick(MinecraftServer server) {
            long now = server.overworld().getGameTime();
            if (now >= nextMaintenanceTick && !PhotoMaintenance.isRunning()) {
                nextMaintenanceTick = now + 36000L;
                PhotoMaintenance.scheduleAutomatic(server, result -> {});
            }
    }

    public static void onServerStarted(MinecraftServer server) {
        PhotoIndexSavedData.get(server);
        nextMaintenanceTick = server.overworld().getGameTime() + 200L;
    }

    public static void onServerStopping() {
        PhotoMaintenance.reset();
        PhotoIoService.shutdown();
        nextMaintenanceTick = 0L;
    }
}

