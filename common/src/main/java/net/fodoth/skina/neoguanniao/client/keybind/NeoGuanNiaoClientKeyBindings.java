package net.fodoth.skina.neoguanniao.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class NeoGuanNiaoClientKeyBindings {
    public static final String KEY_CATEGORY = "key.category." + NeoGuanNiao.MODID + ".bird_guide";
    public static final String KEY_TOGGLE_LAYOUT_EDIT = "key." + NeoGuanNiao.MODID + ".toggle_layout_edit";
    public static final String KEY_SAVE_LAYOUT = "key." + NeoGuanNiao.MODID + ".save_layout";
    public static final String KEY_RELOAD_LAYOUT = "key." + NeoGuanNiao.MODID + ".reload_layout";

    public static final KeyMapping TOGGLE_LAYOUT_EDIT = new KeyMapping(
            KEY_TOGGLE_LAYOUT_EDIT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            KEY_CATEGORY
    );

    public static final KeyMapping SAVE_LAYOUT = new KeyMapping(
            KEY_SAVE_LAYOUT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            KEY_CATEGORY
    );

    public static final KeyMapping RELOAD_LAYOUT = new KeyMapping(
            KEY_RELOAD_LAYOUT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY
    );

}
