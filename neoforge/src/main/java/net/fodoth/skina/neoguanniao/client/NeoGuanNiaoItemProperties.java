package net.fodoth.skina.neoguanniao.client;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanEnchantments;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public final class NeoGuanNiaoItemProperties {
    private NeoGuanNiaoItemProperties() {}

    public static void register() {
        if (!NeoGuanNiaoItems.BIRD_EGG.isPresent() || !NeoGuanNiaoItems.BIRD_FEATHER.isPresent()
                || !NeoGuanNiaoItems.WIND_FEATHER_FAN.isPresent()) return;
        ItemProperties.register(NeoGuanNiaoItems.BIRD_EGG.get(), id("rarity"), (stack, level, entity, seed) -> (float) stack.getOrDefault(NeoGuanNiaoDataComponents.BIRD_EGG_RARITY.get(), BirdSkinRarity.COMMON.getRarity()));
        ItemProperties.register(NeoGuanNiaoItems.BIRD_EGG.get(), id("model_rarity"), (stack, level, entity, seed) -> (float) stack.getOrDefault(NeoGuanNiaoDataComponents.BIRD_EGG_MODEL_RARITY.get(), BirdModelRarity.COMMON.getRarity()));
        ItemProperties.register(NeoGuanNiaoItems.BIRD_EGG.get(), id("gender"), (stack, level, entity, seed) -> (float) stack.getOrDefault(NeoGuanNiaoDataComponents.BIRD_EGG_GENDER.get(), 0));
        ItemProperties.register(NeoGuanNiaoItems.BIRD_FEATHER.get(), id("bird_type"), (stack, level, entity, seed) -> (float) stack.getOrDefault(NeoGuanNiaoDataComponents.BIRD_FEATHER_BIRD_TYPE.get(), 0));
        ItemProperties.register(NeoGuanNiaoItems.WIND_FEATHER_FAN.get(), id("mode"), (stack, level, entity, seed) -> FeatherFanEnchantments.mode(stack));
        ItemProperties.register(NeoGuanNiaoItems.WIND_FEATHER_FAN.get(), id("reserved"), (stack, level, entity, seed) -> FeatherFanItem.isReserved(stack) ? 1.0f : 0.0f);
        ItemProperties.register(NeoGuanNiaoItems.BIRD_FEATHER.get(), id("rarity"), (stack, level, entity, seed) -> (float) stack.getOrDefault(NeoGuanNiaoDataComponents.BIRD_FEATHER_SKIN_RARITY.get(), 0));
    }

    private static ResourceLocation id(String path) { return NeoGuanNiao.resource(path); }
}
