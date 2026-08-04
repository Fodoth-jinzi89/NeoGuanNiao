package net.fodoth.skina.neoguanniao.content.villager.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;


/**
 * QuestShop兼容
 * <p>
 * 当GoldenTweaks和QuestShop同时存在时：
 * emerald -> brilliant_gold
 */
public class QuestShopCompat {


    private static final String GOLDENTWEAKS =
            "goldentweaks";


    private static final String QUESTSHOP =
            "questshop";


    private static final ResourceLocation BRILLIANT_GOLD_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "goldentweaks",
                    "radiant_gold"
            );



    private static Boolean enabled;

    /**
     * 是否启用黄金币替换
     */
    public static boolean isEnabled() {

        if (enabled == null) {

            enabled =
                    ModList.get()
                            .isLoaded(GOLDENTWEAKS)
                            &&
                            ModList.get()
                                    .isLoaded(QUESTSHOP);
        }

        return enabled;
    }



    /**
     * 创建村民交易奖励物品
     * <p>
     * 如果兼容开启:
     * emerald -> brilliant_gold
     */
    public static ItemStack createCurrency(
            int count
    ) {

        if (!isEnabled()) {
            return new ItemStack(
                    Items.EMERALD,
                    count
            );
        }


        Item item =
                BuiltInRegistries.ITEM.get(
                        BRILLIANT_GOLD_ID
                );


        return new ItemStack(
                item,
                count
        );
    }


}