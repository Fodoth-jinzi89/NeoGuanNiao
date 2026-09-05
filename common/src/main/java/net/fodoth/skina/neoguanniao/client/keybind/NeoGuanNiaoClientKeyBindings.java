package net.fodoth.skina.neoguanniao.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public class NeoGuanNiaoClientKeyBindings {
    public static final String KEY_CATEGORY = "key.category." + NeoGuanNiao.MODID + ".bird_guide";
    public static final String KEY_TOGGLE_LAYOUT_EDIT = "key." + NeoGuanNiao.MODID + ".toggle_layout_edit";
    public static final String KEY_SAVE_LAYOUT = "key." + NeoGuanNiao.MODID + ".save_layout";
    public static final String KEY_RELOAD_LAYOUT = "key." + NeoGuanNiao.MODID + ".reload_layout";

    public static final KeyMapping TOGGLE_LAYOUT_EDIT = new KeyMapping(
            KEY_TOGGLE_LAYOUT_EDIT,
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            KEY_CATEGORY
    );

    public static final KeyMapping SAVE_LAYOUT = new KeyMapping(
            KEY_SAVE_LAYOUT,
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            KEY_CATEGORY
    );

    public static final KeyMapping RELOAD_LAYOUT = new KeyMapping(
            KEY_RELOAD_LAYOUT,
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_LAYOUT_EDIT);
        event.register(SAVE_LAYOUT);
        event.register(RELOAD_LAYOUT);
    }
}