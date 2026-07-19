package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class NeoGuanNiaoItemTags {
    public static final TagKey<Item> BIRD_FOOD;
    public static final TagKey<Item> BIRD_BREED_FOOD;

    static {
        BIRD_FOOD = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_food")
        );
        BIRD_BREED_FOOD = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_breed_food")
        );
    }

    public static void register() {
        // 空方法，仅用于触发类加载
    }
}