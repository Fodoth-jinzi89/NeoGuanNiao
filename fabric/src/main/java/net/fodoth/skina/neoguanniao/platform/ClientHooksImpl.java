package net.fodoth.skina.neoguanniao.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientHooksImpl {
    private ClientHooksImpl() {}
    public static void openBirdGuide(ItemStack stack) {
    }
    public static void openCamera(InteractionHand hand) {}
    public static void openPhotograph(ItemStack stack) {}
    public static ConfigHooks.Limits cameraLimits() { return ConfigHooks.Limits.defaults(); }
}
