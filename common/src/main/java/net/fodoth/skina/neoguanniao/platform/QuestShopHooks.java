package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;

public final class QuestShopHooks {
    private QuestShopHooks() {}

    @ExpectPlatform
    public static native ItemStack createCurrency(int count);
}
