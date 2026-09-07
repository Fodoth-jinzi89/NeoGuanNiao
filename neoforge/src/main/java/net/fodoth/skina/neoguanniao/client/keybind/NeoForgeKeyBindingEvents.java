package net.fodoth.skina.neoguanniao.client.keybind;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.camera.CameraKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public final class NeoForgeKeyBindingEvents {
    private NeoForgeKeyBindingEvents() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT.setKeyConflictContext(KeyConflictContext.GUI);
        NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT.setKeyConflictContext(KeyConflictContext.GUI);
        NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT.setKeyConflictContext(KeyConflictContext.GUI);
        event.register(NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT);
        event.register(NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT);
        event.register(NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT);
        event.register(CameraKeyMappings.OPEN_FILTER_LIBRARY);
        event.register(CameraKeyMappings.OPEN_CREATIVE_CONTROLS);
        event.register(CameraKeyMappings.FOCUS);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        NeoGuanNiaoKeyInputHandler.onKeyInput(event.getKey(), event.getAction(),
                NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT.getKey().getValue(),
                NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT.getKey().getValue(),
                NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT.getKey().getValue());
    }
}
