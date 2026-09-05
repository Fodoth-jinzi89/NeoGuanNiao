package com.birdcamera.command;

import com.birdcamera.content.camera.PhotoIndexSavedData;
import com.birdcamera.content.camera.PhotoIoService;
import com.birdcamera.content.camera.PhotoMaintenance;
import com.birdcamera.content.camera.PhotoRepository;
import com.birdcamera.network.PhotoUploadManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * 照片管理命令（/birdcamera photo ...，迁移自 guaniao PhotoAdminCommands）。
 */
public final class PhotoAdminCommands {
    private PhotoAdminCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("birdcamera").requires(source -> source.hasPermission(2))
                                .then(Commands.literal("photo")
                                        .then(Commands.literal("stats")
                                                .executes(context -> stats(context.getSource(), null))
                                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                                        .executes(context -> stats(context.getSource(),
                                                                GameProfileArgument.getGameProfiles(context, "player")
                                                                        .stream().findFirst().orElse(null)))))
                                        .then(Commands.literal("list")
                                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                                        .executes(context -> list(context.getSource(),
                                                                GameProfileArgument.getGameProfiles(context, "player")
                                                                        .stream().findFirst().orElse(null)))))
                                        .then(Commands.literal("delete")
                                                .then(Commands.argument("photo_id", StringArgumentType.word())
                                                        .executes(context -> moveToTrash(context.getSource(),
                                                                StringArgumentType.getString(context, "photo_id")))))
                                        .then(Commands.literal("restore")
                                                .then(Commands.argument("photo_id", StringArgumentType.word())
                                                        .executes(context -> restore(context.getSource(),
                                                                StringArgumentType.getString(context, "photo_id")))))
                                        .then(Commands.literal("verify")
                                                .executes(context -> maintenance(context.getSource(), true)))
                                        .then(Commands.literal("prune")
                                                .then(Commands.literal("dry_run")
                                                        .executes(context -> maintenance(context.getSource(), true)))
                                                .then(Commands.literal("confirm")
                                                        .executes(context -> maintenance(context.getSource(), false)))))));
    }

    private static int stats(CommandSourceStack source, GameProfile profile) {
        UUID owner = profile == null ? null : profile.getId();
        PhotoIndexSavedData.Usage usage = PhotoIndexSavedData.get(source.getServer()).usage(owner);
        source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.stats",
                usage.worldCount(),
                formatBytes(usage.worldBytes()),
                usage.activeCount(),
                usage.trashCount(),
                usage.missingCount(),
                PhotoUploadManager.activeUploads(),
                PhotoUploadManager.activeDownloads(),
                PhotoIoService.queuedTasks()), false);
        if (profile != null) {
            source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.player_stats",
                    profile.getName(), usage.playerCount(), formatBytes(usage.playerBytes())), false);
        }
        return usage.worldCount();
    }

    private static int list(CommandSourceStack source, GameProfile profile) {
        UUID owner = profile == null ? null : profile.getId();
        List<PhotoIndexSavedData.PhotoRecord> owned = PhotoIndexSavedData.get(source.getServer()).ownedBy(owner)
                .stream()
                .sorted(Comparator.comparingLong(PhotoIndexSavedData.PhotoRecord::createdAt).reversed())
                .limit(10)
                .toList();
        source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.list_header",
                profile == null ? "?" : profile.getName(), owned.size()), false);
        for (PhotoIndexSavedData.PhotoRecord record : owned) {
            source.sendSuccess(() -> Component.literal(" - " + record.id() + "  " + formatBytes(record.bytes())
                    + "  " + record.status().name().toLowerCase(Locale.ROOT)), false);
        }
        return owned.size();
    }

    private static int moveToTrash(CommandSourceStack source, String photoId) {
        MinecraftServer server = source.getServer();
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        PhotoIndexSavedData.PhotoRecord record = index.get(photoId);
        if (record == null || record.status() == PhotoIndexSavedData.PhotoStatus.TRASH) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.not_found", photoId));
            return 0;
        }
        boolean accepted = PhotoIoService.submit(server, () -> {
            PhotoRepository.moveToTrash(server, photoId);
            return photoId;
        }, id -> {
            if (index.moveToTrash(photoId, System.currentTimeMillis())) {
                source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.deleted", photoId), true);
            }
        }, throwable ->
                source.sendFailure(Component.translatable("command.birdcamera.photo.io_failed", photoId)));
        if (!accepted) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.busy"));
        }
        return accepted ? 1 : 0;
    }

    private static int restore(CommandSourceStack source, String photoId) {
        MinecraftServer server = source.getServer();
        PhotoIndexSavedData index = PhotoIndexSavedData.get(server);
        PhotoIndexSavedData.PhotoRecord record = index.get(photoId);
        if (record == null || record.status() != PhotoIndexSavedData.PhotoStatus.TRASH) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.not_in_trash", photoId));
            return 0;
        }
        boolean accepted = PhotoIoService.submit(server, () -> {
            PhotoRepository.restoreFromTrash(server, photoId);
            return photoId;
        }, id -> {
            if (index.restore(photoId)) {
                source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.restored", photoId), true);
            }
        }, throwable ->
                source.sendFailure(Component.translatable("command.birdcamera.photo.io_failed", photoId)));
        if (!accepted) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.busy"));
        }
        return accepted ? 1 : 0;
    }

    private static int maintenance(CommandSourceStack source, boolean dryRun) {
        if (PhotoMaintenance.isRunning()) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.busy"));
            return 0;
        }
        boolean accepted = PhotoMaintenance.schedule(source.getServer(), dryRun, result -> {
            if (result.success()) {
                source.sendSuccess(() -> Component.translatable(
                        dryRun ? "command.birdcamera.photo.verify_result" : "command.birdcamera.photo.prune_result",
                        result.storedFiles(), result.missing().size(), result.orphans().size(), result.deletedTrash().size()), true);
            } else {
                source.sendFailure(Component.translatable("command.birdcamera.photo.maintenance_failed", result.error()));
            }
        });
        if (!accepted) {
            source.sendFailure(Component.translatable("command.birdcamera.photo.busy"));
        } else {
            source.sendSuccess(() -> Component.translatable("command.birdcamera.photo.maintenance_started"), false);
        }
        return accepted ? 1 : 0;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        double k = bytes / 1024.0;
        if (k < 1024.0) {
            return String.format(Locale.ROOT, "%.1f KiB", k);
        }
        return String.format(Locale.ROOT, "%.1f MiB", k / 1024.0);
    }
}