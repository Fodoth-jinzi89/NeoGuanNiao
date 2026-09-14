package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.platform.ConfigHooks;
import net.fodoth.skina.neoguanniao.client.camera.CameraClientCapture;
import net.fodoth.skina.neoguanniao.client.camera.PhotographClientActions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientHooksImpl {
    private ClientHooksImpl() {}
    public static void openCamera(InteractionHand hand) {
        CameraClientCapture.openViewfinder(hand);
    }
    public static void openPhotograph(ItemStack stack) {
        PhotographClientActions.openScreen(stack);
    }
}
