package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathItemRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageItemRenderer;
import net.fodoth.skina.neoguanniao.client.camera.FilmItemRenderer;
import net.fodoth.skina.neoguanniao.client.camera.NikonD750ItemRenderer;
import net.fodoth.skina.neoguanniao.client.camera.PhotographItemRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestItemRenderer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.ItemLike;

public final class NeoGuanNiaoFabricItemRenderers {
    private NeoGuanNiaoFabricItemRenderers() {}

    public static void register() {
        register(NeoGuanNiaoItems.SMALL_BIRD_CAGE.get(), new BirdCageItemRenderer(1.2F));
        register(NeoGuanNiaoItems.MEDIUM_BIRD_CAGE.get(), new BirdCageItemRenderer(0.6F));
        register(NeoGuanNiaoItems.LARGE_BIRD_CAGE.get(), new BirdCageItemRenderer(0.5F));
        register(NeoGuanNiaoItems.WOODEN_BIRD_BATH.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.STONE_BIRD_BATH.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.BIRD_BATH.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.WOODEN_BIRD_BATH_2.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.STONE_BIRD_BATH_2.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.BIRD_BATH_2.get(), new BirdBathItemRenderer());
        register(NeoGuanNiaoItems.BIRD_NEST.get(), new BirdNestItemRenderer());
        register(NeoGuanNiaoItems.NIKON_D750.get(), new NikonD750ItemRenderer());
        register(NeoGuanNiaoItems.FILM.get(), new FilmItemRenderer());
        register(NeoGuanNiaoItems.PHOTOGRAPH.get(), new PhotographItemRenderer());
    }

    private static void register(ItemLike item, BlockEntityWithoutLevelRenderer renderer) {
        BuiltinItemRendererRegistry.INSTANCE.register(item, renderer::renderByItem);
    }
}
