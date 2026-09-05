package net.fodoth.skina.neoguanniao.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuestShopHooksImpl {
    private QuestShopHooksImpl() {}

    public static ItemStack createCurrency(int count) {
        return new ItemStack(Items.EMERALD, count);
    }
}
