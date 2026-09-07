package net.fodoth.skina.neoguanniao.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIndexSavedData;
import net.fodoth.skina.neoguanniao.content.camera.PhotoIoService;
import net.fodoth.skina.neoguanniao.content.camera.PhotoMaintenance;
import net.fodoth.skina.neoguanniao.content.camera.PhotoRepository;
import net.fodoth.skina.neoguanniao.platform.PhotoNetworkHooks;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Administrative, read-mostly controls for photo storage. */
public final class PhotoAdminCommands {
    private PhotoAdminCommands() {
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var photo = Commands.literal("photo")
                .then(Commands.literal("stats").executes(ctx -> stats(ctx.getSource(), null))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(ctx -> stats(ctx.getSource(), profile(ctx.getSource(), ctx.getArgument("player", GameProfile.class))))))
                .then(Commands.literal("list").then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> list(ctx.getSource(), ctx.getArgument("player", GameProfile.class)))))
                .then(Commands.literal("delete").then(Commands.argument("photo_id", StringArgumentType.word())
                        .executes(ctx -> moveToTrash(ctx.getSource(), StringArgumentType.getString(ctx, "photo_id")))))
                .then(Commands.literal("restore").then(Commands.argument("photo_id", StringArgumentType.word())
                        .executes(ctx -> restore(ctx.getSource(), StringArgumentType.getString(ctx, "photo_id")))))
                .then(Commands.literal("verify").executes(ctx -> maintenance(ctx.getSource(), true)))
                .then(Commands.literal("prune").then(Commands.literal("dry_run").executes(ctx -> maintenance(ctx.getSource(), true)))
                        .then(Commands.literal("confirm").executes(ctx -> maintenance(ctx.getSource(), false))));
        dispatcher.register(Commands.literal("neoguanniao").requires(source -> source.hasPermission(2)).then(photo));
    }

    private static GameProfile profile(CommandSourceStack source, GameProfile profile) {
        return profile;
    }

    private static int stats(CommandSourceStack source, GameProfile profile) {
        var usage = PhotoIndexSavedData.get(source.getServer()).usage(profile == null ? null : profile.getId());
                source.sendSuccess(() -> Component.translatable("command.neoguanniao.photo.stats", usage.worldCount(), formatBytes(usage.worldBytes()), usage.activeCount(), usage.trashCount(), usage.missingCount(), PhotoNetworkHooks.activeUploads(), PhotoNetworkHooks.activeDownloads(), PhotoIoService.queuedTasks()), false);
        return usage.worldCount();
    }

    private static int list(CommandSourceStack source, GameProfile profile) {
        List<PhotoIndexSavedData.PhotoRecord> records = PhotoIndexSavedData.get(source.getServer()).ownedBy(profile.getId()).stream().sorted(Comparator.comparingLong(PhotoIndexSavedData.PhotoRecord::createdAt).reversed()).limit(10).toList();
        records.forEach(record -> source.sendSuccess(() -> Component.literal(record.id() + "  " + record.status().name().toLowerCase(Locale.ROOT) + "  " + formatBytes(record.bytes())), false));
        return records.size();
    }

    private static int moveToTrash(CommandSourceStack source, String id) {
        try {
            PhotoRepository.moveToTrash(source.getServer(), id);
            PhotoIndexSavedData.get(source.getServer()).moveToTrash(id, System.currentTimeMillis());
            source.sendSuccess(() -> Component.translatable("command.neoguanniao.photo.deleted", id), true);
            return 1;
        } catch (Exception exception) {
            NeoGuanNiao.LOGGER.warn("Unable to move photograph {} to trash", id, exception);
            return 0;
        }
    }

    private static int restore(CommandSourceStack source, String id) {
        try {
            PhotoRepository.restoreFromTrash(source.getServer(), id);
            PhotoIndexSavedData.get(source.getServer()).restore(id);
            source.sendSuccess(() -> Component.translatable("command.neoguanniao.photo.restored", id), true);
            return 1;
        } catch (Exception exception) {
            NeoGuanNiao.LOGGER.warn("Unable to restore photograph {}", id, exception);
            return 0;
        }
    }

    private static int maintenance(CommandSourceStack source, boolean dryRun) {
        return PhotoMaintenance.schedule(source.getServer(), dryRun, result -> source.sendSuccess(
                () -> Component.translatable("command.neoguanniao.photo.maintenance", result.missing().size(), result.orphans().size(), result.deletedTrash().size()), true)) ? 1 : 0;
    }

    private static String formatBytes(long bytes) {
        return bytes < 1_048_576 ? bytes + " B" : String.format(Locale.ROOT, "%.1f MiB", bytes / 1_048_576.0);
    }
}
