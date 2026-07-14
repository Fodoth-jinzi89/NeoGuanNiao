package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class NeoGuanNiaoItemTags {
    public static final TagKey<Item> BIRD_FOOD = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_food")
    );
}
