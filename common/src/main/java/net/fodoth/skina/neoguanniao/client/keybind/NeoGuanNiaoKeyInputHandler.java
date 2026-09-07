package net.fodoth.skina.neoguanniao.client.keybind;

import net.fodoth.skina.neoguanniao.client.guide.BirdGuideScreen;
import net.minecraft.client.Minecraft;

public class NeoGuanNiaoKeyInputHandler {

    public static void onKeyInput(int key, int action, int toggleKey, int saveKey, int reloadKey) {
        if (action != 1) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof BirdGuideScreen screen) {
            if (key == toggleKey) {
                screen.toggleLayoutEditMode();
            } else if (key == saveKey) {
                screen.saveEditedLayout();
            } else if (key == reloadKey) {
                screen.reloadLayout();
            }
        }
    }
}