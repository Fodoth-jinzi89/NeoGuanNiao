package net.fodoth.skina.neoguanniao.config;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public final class NeoGuanNiaoFabricConfigCommand {
    private static boolean openRequested;

    private NeoGuanNiaoFabricConfigCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("neoguanniao-config").executes(context -> {
                    if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
                        context.getSource().sendFeedback(Component.translatable("config.neoguanniao.cloth_required"));
                        return 0;
                    }
                    openRequested = true;
                    return 1;
                })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openRequested) {
                openRequested = false;
                client.setScreen(NeoGuanNiaoConfigScreen.create(client.screen));
            }
        });
    }
}
