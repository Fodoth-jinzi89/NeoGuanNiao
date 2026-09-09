package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fodoth.skina.neoguanniao.client.keybind.NeoGuanNiaoClientKeyBindings;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fodoth.skina.neoguanniao.client.keybind.NeoGuanNiaoKeyInputHandler;
import net.fodoth.skina.neoguanniao.client.camera.CameraKeyMappings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fodoth.skina.neoguanniao.network.FeatherFanPiercePacket;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;

/**
 * Fabric key mapping registration; actions are enabled when the shared guide screen is ported.
 */
public final class NeoGuanNiaoFabricKeyBindings {
    public static final KeyMapping TOGGLE_LAYOUT_EDIT = NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT;
    public static final KeyMapping SAVE_LAYOUT = NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT;
    public static final KeyMapping RELOAD_LAYOUT = NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT;
    public static final KeyMapping FEATHER_FAN_PIERCE = new KeyMapping("key.neoguanniao.feather_fan_pierce", 80, "key.categories.neoguanniao");

    private NeoGuanNiaoFabricKeyBindings() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_LAYOUT_EDIT);
        KeyBindingHelper.registerKeyBinding(SAVE_LAYOUT);
        KeyBindingHelper.registerKeyBinding(RELOAD_LAYOUT);
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.OPEN_FILTER_LIBRARY);
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.OPEN_CREATIVE_CONTROLS);
        KeyBindingHelper.registerKeyBinding(CameraKeyMappings.FOCUS);
        KeyBindingHelper.registerKeyBinding(FEATHER_FAN_PIERCE);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            int toggleKey = TOGGLE_LAYOUT_EDIT.getDefaultKey().getValue();
            int saveKey = SAVE_LAYOUT.getDefaultKey().getValue();
            int reloadKey = RELOAD_LAYOUT.getDefaultKey().getValue();
            if (TOGGLE_LAYOUT_EDIT.consumeClick())
                NeoGuanNiaoKeyInputHandler.onKeyInput(toggleKey, 1, toggleKey, saveKey, reloadKey);
            if (SAVE_LAYOUT.consumeClick())
                NeoGuanNiaoKeyInputHandler.onKeyInput(saveKey, 1, toggleKey, saveKey, reloadKey);
            if (RELOAD_LAYOUT.consumeClick())
                NeoGuanNiaoKeyInputHandler.onKeyInput(reloadKey, 1, toggleKey, saveKey, reloadKey);
            while (FEATHER_FAN_PIERCE.consumeClick()) {
                if (client.player != null && client.player.getUseItem().getItem() instanceof FeatherFanItem
                        && FeatherFanItem.isFullyCharged(client.player)) {
                    ClientPlayNetworking.send(new FeatherFanPiercePacket());
                }
            }
        });
    }

}
