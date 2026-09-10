package net.fodoth.skina.neoguanniao.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "neoguanniao")
public final class NeoForgeCommandEvents {
    private NeoForgeCommandEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PhotoAdminCommands.onRegisterCommands(event.getDispatcher());
    }
}
