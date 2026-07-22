package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class NeoGuanNiaoItemProperties {
    public static void register() {

        ItemProperties.register(
                NeoGuanNiaoItems.BIRD_EGG.get(),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "rarity"),
                (stack, level, entity, seed) ->
                        (float) stack.getOrDefault(
                                NeoGuanNiaoDataComponents.BIRD_EGG_RARITY.get(),
                                BirdSkinRarity.COMMON.getRarity()
                        )
        );

        ItemProperties.register(
                NeoGuanNiaoItems.BIRD_EGG.get(),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "model_rarity"),
                (stack, level, entity, seed) ->
                        (float) stack.getOrDefault(
                                NeoGuanNiaoDataComponents.BIRD_EGG_MODEL_RARITY.get(),
                                BirdModelRarity.COMMON.getRarity()
                        )
        );

        ItemProperties.register(
                NeoGuanNiaoItems.BIRD_EGG.get(),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "gender"),
                (stack, level, entity, seed) ->
                        (float) stack.getOrDefault(
                                NeoGuanNiaoDataComponents.BIRD_EGG_GENDER.get(),
                                0
                        )
        );

        ItemProperties.register(
                NeoGuanNiaoItems.BIRD_FEATHER.get(),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "bird_type"),
                (stack, level, entity, seed) ->
                        (float) stack.getOrDefault(
                                NeoGuanNiaoDataComponents.BIRD_FEATHER_BIRD_TYPE.get(),
                                0
                        )
        );

        ItemProperties.register(
                NeoGuanNiaoItems.BIRD_FEATHER.get(),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "rarity"),
                (stack, level, entity, seed) ->
                        (float) stack.getOrDefault(
                                NeoGuanNiaoDataComponents.BIRD_FEATHER_SKIN_RARITY.get(),
                                0
                        )
        );

    }
}
