package com.birdcamera.event;

import com.birdcamera.content.camera.LegacyPhotoMigration;
import com.birdcamera.content.camera.PhotoIoService;
import com.birdcamera.content.camera.PhotoMaintenance;
import com.birdcamera.network.PhotoUploadManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * 照片传输/维护服务端事件接线（对应原 guaniao PhotoTransferEvents）。
 */
public final class PhotoTransferEvents {
    private static long nextAutomaticMaintenance;

    private PhotoTransferEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PhotoUploadManager.tick(server);
            LegacyPhotoMigration.tick(server);
            long now = System.currentTimeMillis();
            if (now >= nextAutomaticMaintenance && !PhotoMaintenance.isRunning()) {
                nextAutomaticMaintenance = now + 20L * 60 * 1000;
                PhotoMaintenance.scheduleAutomatic(server, result -> {
                });
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PhotoUploadManager.disconnect(handler.player.getUUID()));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PhotoUploadManager.clear();
            LegacyPhotoMigration.clear();
            PhotoMaintenance.reset();
            PhotoIoService.shutdown();
        });
    }
}