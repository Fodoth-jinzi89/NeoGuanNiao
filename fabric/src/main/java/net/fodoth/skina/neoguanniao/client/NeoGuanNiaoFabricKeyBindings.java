package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fodoth.skina.neoguanniao.client.camera.CameraKeyMappings;

/**
 * Fabric key mapping registration for the camera key binds.
 */
public final class NeoGuanNiaoFabricKeyBindings {
    private NeoGuanNiaoFabricKeyBindings() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.OPEN_FILTER_LIBRARY);
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.OPEN_CREATIVE_CONTROLS);
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.FOCUS);
    }

}
