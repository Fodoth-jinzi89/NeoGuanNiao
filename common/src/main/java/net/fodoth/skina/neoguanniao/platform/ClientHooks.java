package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientHooks {
    private ClientHooks() {}
    @ExpectPlatform
    public static void openBirdGuide(ItemStack stack) { throw new AssertionError(); }
    @ExpectPlatform public static void openCamera(InteractionHand hand) { throw new AssertionError(); }
    @ExpectPlatform public static void openPhotograph(ItemStack stack) { throw new AssertionError(); }
}
