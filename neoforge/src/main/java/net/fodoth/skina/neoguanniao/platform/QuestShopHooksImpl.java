package net.fodoth.skina.neoguanniao.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

public final class QuestShopHooksImpl {
    private QuestShopHooksImpl() {}

    public static ItemStack createCurrency(int count) {
        if (!(ModList.get().isLoaded("goldentweaks") && ModList.get().isLoaded("questshop"))) {
            return new ItemStack(Items.EMERALD, count);
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("goldentweaks", "radiant_gold")), count);
    }
}
