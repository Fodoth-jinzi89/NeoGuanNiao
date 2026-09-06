package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientHooks {
    private ClientHooks() {}
    @ExpectPlatform
    public static void openBirdGuide(ItemStack stack) { invoke("openBirdGuide", new Class<?>[]{ItemStack.class}, stack); }
    @ExpectPlatform public static void openCamera(InteractionHand hand) { invoke("openCamera", new Class<?>[]{InteractionHand.class}, hand); }
    @ExpectPlatform public static void openPhotograph(ItemStack stack) { invoke("openPhotograph", new Class<?>[]{ItemStack.class}, stack); }

    private static void invoke(String method, Class<?>[] types, Object... args) {
        try {
            Class.forName("net.fodoth.skina.neoguanniao.platform.neoforge.ClientHooksImpl")
                    .getMethod(method, types).invoke(null, args);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
