package net.fodoth.skina.neoguanniao.client.keybind;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.camera.CameraKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public final class NeoForgeKeyBindingEvents {
    private NeoForgeKeyBindingEvents() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CameraKeyMappings.OPEN_FILTER_LIBRARY);
        event.register(CameraKeyMappings.OPEN_CREATIVE_CONTROLS);
        event.register(CameraKeyMappings.FOCUS);
    }
}
