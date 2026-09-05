package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;

public final class ClientHooks {
    private ClientHooks() {}
    @ExpectPlatform
    public static native void openBirdGuide(ItemStack stack);
    @ExpectPlatform public static native void openCamera(net.minecraft.world.InteractionHand hand);
    @ExpectPlatform public static native void openPhotograph(ItemStack stack);
}
