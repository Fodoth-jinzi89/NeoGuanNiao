package net.fodoth.skina.neoguanniao.content.villager.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Common fallback; platform modules may provide optional currency integration. */
public final class QuestShopCompat {
    private QuestShopCompat() {}
    public static boolean isEnabled() { return false; }
    public static ItemStack createCurrency(int count) { return new ItemStack(Items.EMERALD, count); }
}
