package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.platform.ConfigHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.fodoth.skina.neoguanniao.client.guide.BirdGuideClient;
import net.fodoth.skina.neoguanniao.client.camera.CameraClientCapture;
import net.fodoth.skina.neoguanniao.client.camera.PhotographClientActions;

public final class ClientHooksImpl {
    private ClientHooksImpl() {
    }

    public static void openBirdGuide(ItemStack stack) {
        BirdGuideClient.open(stack);
    }

    public static void openCamera(InteractionHand hand) {
        CameraClientCapture.openViewfinder(hand);
    }

    public static void openPhotograph(ItemStack stack) {
        PhotographClientActions.openScreen(stack);
    }

    public static ConfigHooks.Limits cameraLimits() {
        return ConfigHooks.Limits.defaults();
    }
}
