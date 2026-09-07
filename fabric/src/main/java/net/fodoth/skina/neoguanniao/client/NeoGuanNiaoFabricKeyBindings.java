package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fodoth.skina.neoguanniao.client.keybind.NeoGuanNiaoClientKeyBindings;
import net.minecraft.client.KeyMapping;

/** Fabric key mapping registration; actions are enabled when the shared guide screen is ported. */
public final class NeoGuanNiaoFabricKeyBindings {
    public static final KeyMapping TOGGLE_LAYOUT_EDIT = NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT;
    public static final KeyMapping SAVE_LAYOUT = NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT;
    public static final KeyMapping RELOAD_LAYOUT = NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT;

    private NeoGuanNiaoFabricKeyBindings() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_LAYOUT_EDIT);
        KeyBindingHelper.registerKeyBinding(SAVE_LAYOUT);
        KeyBindingHelper.registerKeyBinding(RELOAD_LAYOUT);
    }

}
