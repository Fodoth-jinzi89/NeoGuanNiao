package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class NeoGuanNiaoItemTags {
    public static final TagKey<Item> BIRD_FOOD;
    public static final TagKey<Item> BIRD_FOOD_FISH;
    public static final TagKey<Item> BIRD_BREED_FOOD;
    public static final TagKey<Item> BIRD_BREED_FOOD_FISH;

    static {
        BIRD_FOOD = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_food")
        );
        BIRD_FOOD_FISH = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_food_fish")
        );
        BIRD_BREED_FOOD = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_breed_food")
        );
        BIRD_BREED_FOOD_FISH = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_breed_food_fish")
        );
    }

    public static void register() {
        // 空方法，仅用于触发类加载
    }
}