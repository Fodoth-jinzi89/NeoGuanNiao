package net.fodoth.skina.neoguanniao.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Fabric key mapping registration; actions are enabled when the shared guide screen is ported. */
public final class NeoGuanNiaoFabricKeyBindings {
    private static final String CATEGORY = "key.category." + NeoGuanNiao.MODID + ".bird_guide";
    public static final KeyMapping TOGGLE_LAYOUT_EDIT = mapping("toggle_layout_edit", GLFW.GLFW_KEY_E);
    public static final KeyMapping SAVE_LAYOUT = mapping("save_layout", GLFW.GLFW_KEY_S);
    public static final KeyMapping RELOAD_LAYOUT = mapping("reload_layout", GLFW.GLFW_KEY_R);

    private NeoGuanNiaoFabricKeyBindings() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_LAYOUT_EDIT);
        KeyBindingHelper.registerKeyBinding(SAVE_LAYOUT);
        KeyBindingHelper.registerKeyBinding(RELOAD_LAYOUT);
    }

    private static KeyMapping mapping(String id, int key) {
        return new KeyMapping("key." + NeoGuanNiao.MODID + "." + id,
                InputConstants.Type.KEYSYM, key, CATEGORY);
    }
}
