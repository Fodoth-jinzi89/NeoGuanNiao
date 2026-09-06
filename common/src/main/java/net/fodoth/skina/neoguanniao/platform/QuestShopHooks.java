package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuestShopHooks {
    private QuestShopHooks() {}

    @ExpectPlatform
    public static ItemStack createCurrency(int count) { return new ItemStack(Items.EMERALD, count); }
}
