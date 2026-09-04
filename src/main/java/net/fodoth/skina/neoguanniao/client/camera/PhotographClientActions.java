package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.PhotographScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public final class PhotographClientActions {
    private PhotographClientActions() {
    }

    public static void openScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen((Screen)new PhotographScreen(stack));
    }
}

