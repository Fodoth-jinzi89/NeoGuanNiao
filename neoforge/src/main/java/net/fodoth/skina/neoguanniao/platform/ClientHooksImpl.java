package net.fodoth.skina.neoguanniao.platform;

import net.fodoth.skina.neoguanniao.client.guide.BirdGuideClient;
import net.minecraft.world.item.ItemStack;

public final class ClientHooksImpl {
    private ClientHooksImpl() {}
    public static void openBirdGuide(ItemStack stack) {
        BirdGuideClient.open(stack);
    }
}
