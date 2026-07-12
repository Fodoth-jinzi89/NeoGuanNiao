package net.fodoth.skina.neoguanniao.client.keybind;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.guide.BirdGuideScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public class NeoGuanNiaoKeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != 1) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof BirdGuideScreen screen) {
            int key = event.getKey();

            if (key == NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT.getKey().getValue()) {
                screen.toggleLayoutEditMode();
            } else if (key == NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT.getKey().getValue()) {
                screen.saveEditedLayout();
            } else if (key == NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT.getKey().getValue()) {
                screen.reloadLayout();
            }
        }
    }
}